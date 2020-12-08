package PlayerBehaviours;

import agents.Player;
import data.Team;
import data.message.PlayerActionMessage;
import jade.core.AID;
import sajas.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import uchicago.src.sim.network.DefaultDrawableNode;
import uchicago.src.sim.network.Edge;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

import static data.AgentType.PLAYER;
import static data.MessageType.ATTACK;

public class AttackingBehaviour extends WakerBehaviour {
    private final Player agent;
    private boolean canAttack;

    private static final int MIN_DMG = 20;
    private static final int MAX_DMG = 40;

    private final static int DEFENDER_DEFENDING_ATTACK_TIMEOUT = 500;
    private final static int DEFENDER_ATTACKING_ATTACK_TIMEOUT = 1000;
    private final static int ASSAULT_DEFENDING_ATTACK_TIMEOUT = 1000;
    private final static int ASSAULT_ATTACKING_ATTACK_TIMEOUT = 500;
    private final static int MEDIC_ATTACK_TIMEOUT = 1000;
    private final static int SNIPER_ATTACK_TIMEOUT = 2000;

    // Independent variables
    public static int ASSAULT_ATTACK_FACTOR = 1;
    public static int DEFENDER_ATTACK_FACTOR = 1;
    public static int MEDIC_ATTACK_FACTOR = 1;
    public static int SNIPER_ATTACK_FACTOR = 3;

    public AttackingBehaviour(Player agent) {
        super(agent, 0);
        this.agent = agent;
        this.canAttack = true;
    }

    public AttackingBehaviour(Player agent, AID enemy) {
        super(agent, getTimeout(agent) / agent.speedFactor);
        this.agent = agent;
        this.attack(enemy);
        this.canAttack = false;
    }

    public static int getTimeout(Player agent) {
        double zonePoints = agent.getCapturableZones().get(agent.getCurrentZone());

        switch (agent.getPlayerClass()) {
            case ASSAULT:
                return (zonePoints == -100 && agent.getTeam() == Team.AXIS)
                        || (zonePoints == 100 && agent.getTeam() == Team.ALLIED)
                        ? ASSAULT_DEFENDING_ATTACK_TIMEOUT
                        : ASSAULT_ATTACKING_ATTACK_TIMEOUT;
            case DEFENDER:
                return (zonePoints == -100 && agent.getTeam() == Team.AXIS)
                        || (zonePoints == 100 && agent.getTeam() == Team.ALLIED)
                        ? DEFENDER_ATTACKING_ATTACK_TIMEOUT
                        : DEFENDER_DEFENDING_ATTACK_TIMEOUT;
            case MEDIC: return MEDIC_ATTACK_TIMEOUT;
            case SNIPER: return SNIPER_ATTACK_TIMEOUT;
            default: return 0;
        }
    }

    @Override
    public void onWake() {
        this.setCanAttack(true);
    }

    @Override
    public void reset(long timeout) {
        super.reset(timeout);
    }

    /**
     * Attacks another player agent
     * @param enemy: AID
     */
    public void attack(AID enemy) {
        Random rand = new Random();

        // The player can miss the attack
        if(!rand.nextBoolean()) return;

        int damage = this.getDamage(rand.nextInt(MAX_DMG - MIN_DMG + 1) + MIN_DMG);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        PlayerActionMessage content = new PlayerActionMessage(PLAYER, ATTACK, agent.getCurrentZone(), -damage);

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(enemy);

        this.sendMessageDrawEdge(enemy.getLocalName());
        this.agent.send(msg);
        this.agent.setPoints(this.agent.getPoints() + damage);
        this.agent.getSwingGUIGame().getTeamCompPanel().addUpdateTeamPlayer(this.agent.getTeam(), this.agent.getAID(), this.agent.getPoints(), this.agent.getPlayerClass());
        this.agent.logAction(this.agent.getLocalName() + " attacking " + enemy.getLocalName() + " for " + damage + "hp");
    }

    public void sendMessageDrawEdge(String agentName){
        if(this.agent.getMyNode() != null) {
           /* DefaultDrawableNode to = Launcher.getNode(agentName);
            Edge edge = new Edge(this.agent.getMyNode(), to);
            edge.setColor(Color.RED);
            myNode.addOutEdge(edge);*/
        }
    }

    public int getDamage(int damage) {
        switch (agent.getPlayerClass()) {
            case ASSAULT: return damage * ASSAULT_ATTACK_FACTOR;
            case DEFENDER: return damage * DEFENDER_ATTACK_FACTOR;
            case MEDIC: return damage * MEDIC_ATTACK_FACTOR;
            case SNIPER: return damage * SNIPER_ATTACK_FACTOR;
            default: return damage;
        }
    }

    public boolean canAttack() {
        return this.canAttack;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }
}
