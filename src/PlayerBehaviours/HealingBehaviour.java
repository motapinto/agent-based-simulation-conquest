package PlayerBehaviours;

import agents.Player;
import data.message.PlayerActionMessage;
import jade.core.AID;
import sajas.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import uchicago.src.sim.network.DefaultDrawableEdge;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

import static data.AgentType.PLAYER;
import static data.MessageType.HEAL;

public class HealingBehaviour extends WakerBehaviour {
    private final Player agent;
    private int missingHP;
    private boolean canHeal;

    private static final int MIN_HEAL = 25;
    private static final int MAX_HEAL = 50;
    private static final int HEALING_TIMEOUT = 8000;

    // Independent variables
    public static int MEDIC_HEALTH = 150;
    public static int ASSAULT_HEALTH = 150;
    public static int SNIPER_HEALTH = 100;
    public static int DEFENDER_HEALTH = 200;

    public HealingBehaviour(Player agent) {
        super(agent, 0);
        this.agent = agent;
        this.canHeal = true;
    }

    public HealingBehaviour(Player agent, AID ally, int missingHP) {
        super(agent, HEALING_TIMEOUT / agent.speedFactor);
        this.agent = agent;
        this.missingHP = missingHP;
        this.heal(ally);
        this.canHeal = false;
    }

    @Override
    public void onWake() {
        this.setCanHeal(true);
    }

    @Override
    public void reset(long timeout) {
        super.reset(timeout);
    }

    /**
     * Heals another player agent
     * @param ally: agents.PlayerAgent to be healed
     */
    public void heal(AID ally) {
        Random rand = new Random();
        int repairment = Math.min(rand.nextInt(MAX_HEAL - MIN_HEAL + 1) + MIN_HEAL, this.missingHP);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        PlayerActionMessage content = new PlayerActionMessage(PLAYER, HEAL, this.agent.getCurrentZone(), repairment);

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(ally);
        this.sendMessageDrawEdge(ally.getLocalName());
        this.agent.send(msg);
        this.agent.setPoints(this.agent.getPoints() + repairment);
        this.agent.getSwingGUIGame().getTeamCompPanel().addUpdateTeamPlayer(this.agent.getTeam(), this.agent.getAID(), this.agent.getPoints(), this.agent.getPlayerClass());
        this.agent.logAction(this.agent.getLocalName() + " healing " + ally.getLocalName() + " for " + repairment + "hp");
    }

    public void sendMessageDrawEdge(String agentName){
        if(this.agent.getMyNode() != null) {
            DefaultDrawableNode to = Execute.Launcher.getNode(agentName);
            DefaultDrawableEdge edge = new DefaultDrawableEdge(this.agent.getMyNode(), to);
            edge.setLabel("HEAL");
            edge.setDrawDirected(true);
            edge.setStrength(5);
            edge.setColor(Color.GREEN);
            this.agent.getMyNode().addOutEdge(edge);
        }
    }

    public boolean canHeal() {
        return canHeal;
    }

    public void setCanHeal(boolean canHeal) {
        this.canHeal = canHeal;
    }
}
