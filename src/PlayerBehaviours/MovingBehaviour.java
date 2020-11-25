package PlayerBehaviours;

import agents.Player;
import data.MovementType;
import data.Position;
import data.message.MovingMessage;
import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static data.AgentType.PLAYER;
import static data.MovementType.ENTERED;
import static data.MovementType.LEFT;

public class MovingBehaviour extends WakerBehaviour {
    private final AID zone;
    private final Player agent;
    private final MovementType moveType;

    public static final int RESPAWN_TIME = 5000;
    private static final int TRAVEL_TIME_FACTOR = 200;
    private static final int IN_ZONE_TICK = 1000;

    public MovingBehaviour(Player agent, AID zone, MovementType moveType) {
        super(agent, getTimeout(agent, zone, moveType));
        this.agent = agent;
        this.zone = zone;
        this.agent.getSwingGUIGame().getZoneInformationPanel().addPlayerMovingToZone(this.agent.getAID(), this.zone, 10);
        this.moveType = moveType;
    }

    public static long getTimeout(Player agent, AID zone, MovementType moveType) {
        switch (moveType) {
            case ENTERED: {
                if(agent.getSpawnZone().equals(zone)){
                    return RESPAWN_TIME;
                }

                Random rand = new Random();
                double randomValue = 0.9 + (1.1 - 0.9) * rand.nextDouble();

                Position currentZonePos = agent.getPositionsZones().get(agent.getCurrentZone());
                double travelTime = agent.getPositionsZones().get(zone).calculateDistance(currentZonePos) / agent.getVelocity();
                return (long) (travelTime * TRAVEL_TIME_FACTOR * randomValue);
            }
            case LEFT: default: return 0;
        }
    }

    @Override
    public void onWake() {
        if(moveType == MovementType.ENTERED) {
            this.moveIn(zone);
        } else if(moveType == LEFT) {
            this.moveOut(zone);
        }
    }

    @Override
    public void reset(long timeout) {
        super.reset(timeout);
    }

    /**
     * Warns zone agent when player agent enters a zone
     */
    public void moveIn(AID zone) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        MovingMessage content = new MovingMessage(PLAYER, this.agent.getTeam(), ENTERED, this.agent.getAID());

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(zone);
        this.agent.send(msg);
        this.agent.setCurrentZone(zone);
        this.agent.getSwingGUIGame().getZoneInformationPanel().removePlayerMovingToZone(this.agent.getAID());
        this.agent.getSwingGUIGame().getZoneInformationPanel().addUpdatePlayer(this.agent);
        this.agent.getZonesBackup().computeIfPresent(zone, (k, v) -> false);

        InZoneBehaviour inZone = new InZoneBehaviour(this.agent, IN_ZONE_TICK);
        this.agent.setInZoneBehaviour(inZone);
        this.agent.addBehaviour(inZone);
    }

    /**
     * Warns zone agent when player agent leaves a zone
     */
    public void moveOut(AID zone) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        MovingMessage content = new MovingMessage(PLAYER, this.agent.getTeam(), LEFT, this.agent.getAID());

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.agent.removeBehaviour(this.agent.getInZoneBehaviour());
        this.cleanZoneInfo();

        this.agent.setCurrentZone(null);
        this.agent.getSwingGUIGame().getZoneInformationPanel().addUpdatePlayer(this.agent);
        msg.addReceiver(zone);
        this.agent.send(msg);
    }

    public void cleanZoneInfo() {
        this.agent.setTeamPlayersInZone(new ArrayList<>());
        this.agent.setEnemyPlayersInZone(new ArrayList<>());
        this.agent.setTeamPlayersInZoneHealth(new HashMap<>());
    }
}
