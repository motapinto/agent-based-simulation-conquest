package agents;

import PlayerBehaviours.*;
import SharedBehaviours.GameServerSubscriber;
import data.MovementType;
import data.PlayerClass;
import data.Position;
import data.Team;
import gui.SwingGUIGame;
import gui.SwingGUIStats;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static PlayerBehaviours.HealingBehaviour.*;

public class Player extends Logger {
    private int health;
    private final Team team;
    private final PlayerClass playerClass;
    private int points = 0;
    private double velocity = 1;
    private final AID gameServerAgent = new sajas.core.AID("game-server", false);
    private long moveTimeout = 0;
    public final int speedFactor;

    DefaultDrawableNode myNode;

    private AID currentZone;
    private AID spawnZone;
    private List<AID> teamPlayers = new ArrayList<>();
    private final Map<AID, Double> capturableZones = new HashMap<>();
    private final Map<AID, Double> deltaZonePoints = new HashMap<>();
    private final Map<AID, Position> positionsZones = new HashMap<>();
    private final Map<AID, Boolean> zonesBackup = new HashMap<>();

    private List<AID> teamPlayersInZone = new ArrayList<>();
    private List<AID> enemyPlayersInZone = new ArrayList<>();
    private Map<AID, Integer> teamPlayersInZoneHealth = new HashMap<>();
    private InZoneBehaviour inZoneBehaviour;


    public Player(Team team, PlayerClass playerClass, SwingGUIGame swingGUIGame, SwingGUIStats swingGUIStats, int speedFactor) {
        super(swingGUIGame, swingGUIStats);
        this.team = team;
        this.playerClass = playerClass;
        this.setInitialAgentStats();
        this.speedFactor = speedFactor;
    }

    public void setInitialAgentStats() {
        switch (this.playerClass) {
            case ASSAULT:
                this.velocity = MovingBehaviour.ASSAULT_VELOCITY;
                this.health = ASSAULT_HEALTH;
                break;
            case MEDIC:
                this.velocity = MovingBehaviour.MEDIC_VELOCITY;
                this.health = HealingBehaviour.MEDIC_HEALTH;
                break;
            case SNIPER:
                this.velocity = MovingBehaviour.SNIPER_VELOCITY;
                this.health = HealingBehaviour.SNIPER_HEALTH;
                break;
            case DEFENDER:
                this.velocity = MovingBehaviour.DEFENDER_VELOCITY;
                this.health = HealingBehaviour.DEFENDER_HEALTH;
                break;
        }
    }

    @Override
    public void setup() {
        if (this.team == Team.ALLIED) this.registerDF("allied-player");
        else this.registerDF("axis-player");

        this.addBehaviour(new ListeningBehaviour(this));
        this.addBehaviour(new GameServerSubscriber(this, this.gameServerAgent));
    }

    /**
     * Procedures to initialize the game
     */
    public void init() {
        this.spawnZone = this.getSpawnLocation();

        List<AID> zones = this.searchCapturableZones();
        zones.forEach(zone -> this.capturableZones.put(zone, 0.0));
        zones.forEach(zone -> this.zonesBackup.put(zone, false));
        zones.forEach(zone -> this.deltaZonePoints.put(zone, 0.0));
        this.addBehaviour(new ZoneSubscriber(this));
        swingGUIGame.getTeamCompPanel().addUpdateTeamPlayer(this.team, this.getAID(), 0, this.playerClass);
    }

    /**
     * Procedures to the start of the game
     */
    public void start() {
        this.addBehaviour(new MovingBehaviour(this, this.spawnZone, MovementType.ENTERED));
        this.teamPlayers = this.getTeamPlayers();
    }

    /**
     * Procedures for the end of the game
     */
    public void end() {
        super.end();
    }

    /**
     * Search on the Directory Facilitator(DF) for all zones services
     */
    public List<AID> searchCapturableZones() {
        List<AID> zones = new ArrayList<>();

        DFAgentDescription[] zoneDescriptions = this.searchDF("zone");
        for(DFAgentDescription zone : zoneDescriptions) {
            zones.add(zone.getName());
        }

        return zones;
    }

    /**
     * Search on the Directory Facilitator(DF) for the zone agent
     * responsible for the spawn location
     */
    private AID getSpawnLocation() {
        DFAgentDescription zone = null;
        if(this.team == Team.ALLIED) {
            zone = this.searchDF("allied-spawn")[0];
        } else if (this.team == Team.AXIS) {
            zone = this.searchDF("axis-spawn")[0];
        }

        assert zone != null;
        return zone.getName();
    }

    /**
     * Search on the Directory Facilitator(DF) for all team players
     */
    public List<AID> getTeamPlayers() {
        List<AID> teamPlayers = new ArrayList<>();

        String query;
        if(this.team == Team.ALLIED) {
            query = "allied";
        } else {
            query = "axis";
        }

        DFAgentDescription[] allies = this.searchDF(String.format("%s-player", query));
        for(DFAgentDescription ally : allies) {
            if(!ally.getName().equals(this.getAID())) {
                teamPlayers.add(ally.getName());
            }
        }

        return teamPlayers;
    }

    /**
     * Send message to all team members that are not currently on the same zone as the agent
     */
    public void sendMessageToTeamMembersNotInZone(ACLMessage msg) {
        for(AID member : this.teamPlayers) {
            if(!this.teamPlayersInZone.contains(member)) {
                msg.addReceiver(member);
            }
        }
        this.send(msg);
    }

    /**
     * Send message to all team members that are currently on the same zone as the agent
     */
    public void sendMessageToTeamMembersInZone(ACLMessage msg) {
        for(AID member : this.teamPlayers) {
            if(this.teamPlayersInZone.contains(member)) {
                msg.addReceiver(member);
            }
        }
        this.send(msg);
    }

    public Team getTeam() {
        return team;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public AID getGameServerAgent() {
        return gameServerAgent;
    }

    public AID getCurrentZone() {
        return currentZone;
    }

    public void setCurrentZone(AID currentZone) {
        this.currentZone = currentZone;
    }

    public AID getSpawnZone() {
        return spawnZone;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(Math.min(health, getMaxHealth(this.playerClass)), 0);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getVelocity() {
        return velocity;
    }

    public Map<AID, Double> getCapturableZones() {
        return capturableZones;
    }

    public Map<AID, Position> getPositionsZones() {
        return positionsZones;
    }

    public List<AID> getTeamPlayersInZone() {
        return teamPlayersInZone;
    }

    public void setTeamPlayersInZone(List<AID> teamPlayersInZone) {
        this.teamPlayersInZone = teamPlayersInZone;
    }

    public List<AID> getEnemyPlayersInZone() {
        return enemyPlayersInZone;
    }

    public void setEnemyPlayersInZone(List<AID> enemyPlayersInZone) {
        this.enemyPlayersInZone = enemyPlayersInZone;
    }

    public Map<AID, Boolean> getZonesBackup() {
        return zonesBackup;
    }

    public Map<AID, Integer> getTeamPlayersInZoneHealth() {
        return teamPlayersInZoneHealth;
    }

    public Map<AID, Double> getDeltaZonePoints() {
        return deltaZonePoints;
    }

    public InZoneBehaviour getInZoneBehaviour() {
        return inZoneBehaviour;
    }

    public void setInZoneBehaviour(InZoneBehaviour inZoneBehaviour) {
        this.inZoneBehaviour = inZoneBehaviour;
    }

    public void setTeamPlayersInZoneHealth(Map<AID, Integer> teamPlayersInZoneHealth) {
        this.teamPlayersInZoneHealth = teamPlayersInZoneHealth;
    }

    public DefaultDrawableNode getMyNode() {
        return myNode;
    }

    public long getMoveTimeout() {
        return moveTimeout;
    }

    public void setMoveTimeout(long moveTimeout) {
        this.moveTimeout = moveTimeout;
    }

    public static int getMaxHealth(PlayerClass playerClass) {
        switch (playerClass) {
            case ASSAULT:
                return ASSAULT_HEALTH;
            case SNIPER:
                return SNIPER_HEALTH;
            case MEDIC:
                return MEDIC_HEALTH;
            case DEFENDER:
                return DEFENDER_HEALTH;
        }

        return 100;
    }

    public void setNode(DefaultDrawableNode node) {
        this.myNode = node;
    }
}
