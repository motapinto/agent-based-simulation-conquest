package agents;

import SharedBehaviours.GameServerSubscriber;
import ZoneBehaviours.CapturingBehaviour;
import ZoneBehaviours.InformPositionBehaviour;
import ZoneBehaviours.ListeningBehaviour;
import data.Position;
import data.Team;
import data.ZoneType;
import gui.SwingGUIGame;
import gui.SwingGUIStats;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

import java.util.ArrayList;
import java.util.List;

public class Zone extends Logger {
    private int numberOfAxisPlayers = 0;
    private int numberOfAlliedPlayers = 0;
    private double capturePoints = 0;
    private Team zoneTeam;
    public ZoneType zoneType;
    public final Position position;
    private final int timeToBeCaptured;

    private ArrayList<AID> alliedAgents = new ArrayList<>();
    private ArrayList<AID> axisAgents = new ArrayList<>();
    private final AID gameServerAgent;
    private SubscriptionResponder subscriptionResponder;
    public List<AID> playerAgents = new ArrayList<>();

    public Zone(Position position, ZoneType zoneType, Team team, int timeToBeCaptured, SwingGUIGame swingGUIGame, SwingGUIStats swingGUIStats) {
        super(swingGUIGame, swingGUIStats);
        this.zoneTeam = team;
        this.position = position;
        this.zoneType = zoneType;
        this.timeToBeCaptured = timeToBeCaptured;
        this.gameServerAgent = new AID("game-server", false);

    }

    @Override
    public void setup() {
        // Register the zone in the Directory Facilitator
        if(this.zoneTeam == Team.ALLIED && this.zoneType == ZoneType.BASE)
            registerDF("allied-spawn");
        else if(this.zoneTeam == Team.AXIS && this.zoneType == ZoneType.BASE)
            registerDF("axis-spawn");
        else
            registerDF("zone");

        // Add zone behaviours
        addBehaviour(new ListeningBehaviour(this));
        addBehaviour(new GameServerSubscriber(this, gameServerAgent));

        // Create subscription behaviour for zone
        MessageTemplate template = new MessageTemplate(
                (MessageTemplate.MatchExpression) aclMessage -> aclMessage.getPerformative() == ACLMessage.SUBSCRIBE
        );
        //Initialize subscription service
        subscriptionResponder = new SubscriptionResponder(this, template);
        addBehaviour(subscriptionResponder);
    }

    /**
     * Procedures for the start of the game
     */
    public void init(){
        this.doWake();
        this.playerAgents = this.findPlayerAgents();
        if(this.zoneTeam == Team.NEUTRAL)
            this.addBehaviour(new CapturingBehaviour(this, 250));
        this.addBehaviour(new InformPositionBehaviour(this));
        this.swingGUIGame.getZoneMapPanel().addUpdateZone(this.getAID(), this.zoneTeam, this.position, this.capturePoints);
        this.swingGUIGame.getZoneInformationPanel().addUpdateNewZone(this.getAID(), this.zoneTeam, this.position);
    }

    /**
     * Procedures for the end of the game
     */
    public void end(){
        if(zoneType != ZoneType.BASE)
            this.zoneTeam = Team.NEUTRAL;
        this.numberOfAlliedPlayers = 0;
        this.numberOfAxisPlayers = 0;
        this.capturePoints = 0;
        this.doSuspend();
    }

    /**
     * Find all player agents registered on the Directory Facilitator(DF)
     * @return List of player agents found on the Directory Facilitator(DF)
     */
    private List<AID> findPlayerAgents(){
        List<AID> playerAgents = new ArrayList<>();
        DFAgentDescription[] alliesPlayers = this.searchDF("allied-player");
        DFAgentDescription[] axisPlayers = this.searchDF("axis-player");

        for(DFAgentDescription ally : alliesPlayers) {
            playerAgents.add(ally.getName());
        }
        for(DFAgentDescription axis : axisPlayers) {
            playerAgents.add(axis.getName());
        }

        return playerAgents;
    }

    /**
     * Increases the number of players of a certain team
     * @param team team of the player that entered the zone
     */
    public void playerEnteredZone(Team team, AID agent){
        switch (team){
            case  ALLIED: this.alliedAgents.add(agent); break;
            case  AXIS: this.axisAgents.add(agent); break;
        }
    }

    /**
     * Decreases the number of players of a certain team
     * @param team team of the player that left the zone
     */
    public void playerLeftZone(Team team, AID agent){
        switch (team){
            case ALLIED: this.alliedAgents.remove(agent); break;
            case AXIS: this.axisAgents.remove(agent); break;
        }
    }

    public void decreaseCapturePoints(double numberOfPoints){
        this.capturePoints -= numberOfPoints;
    }

    public void increaseCapturePoints(double numberOfPoints){
        this.capturePoints += numberOfPoints;
    }

    public void setTeam(Team team) {
        this.zoneTeam = team;
    }

    public Team getZoneTeam() { return zoneTeam; }

    public int getTimeToBeCaptured() {
        return timeToBeCaptured;
    }

    public AID getGameServerAgent() {
        return gameServerAgent;
    }

    public List<AID> getPlayerAgents() { return playerAgents; }

    public Position getPosition() { return position; }

    public double getCapturePoints() { return capturePoints; }

    public void setCapturePoints(double capturePoints) { this.capturePoints = capturePoints; }

    public SubscriptionResponder getSubscriptionResponder() { return subscriptionResponder; }

    public ArrayList<AID> getAlliedAgents() { return alliedAgents; }

    public ArrayList<AID> getAxisAgents() { return axisAgents; }
}
