package agents;

import data.Team;
import gameServerBehaviours.GameServerReceiver;
import gameServerBehaviours.GameServerStart;
import gui.SwingGUIGame;
import gui.SwingGUIStats;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

import java.util.HashMap;
import java.util.Map;

public class GameServer extends Logger {
    private final int zoneNumber; // number of zones
    private final int playersPerTeam; // number of players per team
    private final Map<AID, Team> zones = new HashMap<>(); // number of zones
    private final int initialTickets; // number of tickets per team on game start
    private final int gameTime; // Maximum time a game can take to finish
    private SubscriptionResponder subscriptionResponder;
    private boolean finished = false;

    public GameServer(int zoneNumber, int playersPerTeam, int initialTickets, int gameTime, SwingGUIGame swingGUIGame, SwingGUIStats swingGUIStats) {
        super(swingGUIGame, swingGUIStats);
        this.zoneNumber = zoneNumber;
        this.playersPerTeam = playersPerTeam;
        this.initialTickets = initialTickets;
        this.gameTime = gameTime;
    }

    public void setup() {
        // Create subscription behaviour for game server
        MessageTemplate template = SubscriptionResponder.createMessageTemplate(ACLMessage.SUBSCRIBE);

        //Initialize subscription service
        subscriptionResponder = new SubscriptionResponder(this, template);

        //Add behaviours
        addBehaviour(subscriptionResponder);
        addBehaviour(new GameServerReceiver(this, new ACLMessage(ACLMessage.SUBSCRIBE)));
        addBehaviour(new GameServerStart(this));

        registerDF("game-server");

        this.logConfig(this.getLocalName() + " initialized");
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public int getInitialTickets() {
        return initialTickets;
    }

    public Map<AID, Team> getZones() {
        return zones;
    }

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }

    public SubscriptionResponder getSubscriptionResponder() {
        return subscriptionResponder;
    }

    public int getGameTime() {
        return gameTime;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public void init() {

    }

    @Override
    public void end() {

    }
}
