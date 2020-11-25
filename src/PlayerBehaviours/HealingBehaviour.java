package PlayerBehaviours;

import agents.Player;
import data.message.PlayerActionMessage;
import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Random;

import static data.AgentType.PLAYER;
import static data.MessageType.HEAL;

public class HealingBehaviour extends WakerBehaviour {
    private final Player agent;
    private boolean canHeal;

    private static final int MIN_HEAL = 50;
    private static final int MAX_HEAL = 100;
    private static final int HEALING_TIMEOUT = 5000;

    public HealingBehaviour(Player agent) {
        super(agent, 0);
        this.agent = agent;
        this.canHeal = true;
    }

    public HealingBehaviour(Player agent, AID ally) {
        super(agent, HEALING_TIMEOUT);
        this.agent = agent;
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
        int repairment = rand.nextInt(MAX_HEAL - MIN_HEAL + 1) + MIN_HEAL;

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        PlayerActionMessage content = new PlayerActionMessage(PLAYER, HEAL, this.agent.getCurrentZone(), repairment);

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(ally);
        this.agent.send(msg);
        this.agent.setPoints(this.agent.getPoints() + repairment);
        this.agent.getSwingGUIGame().getTeamCompPanel().addUpdateTeamPlayer(this.agent.getTeam(), this.agent.getAID(), this.agent.getPoints(), this.agent.getPlayerClass());
        this.agent.logAction(this.agent.getLocalName() + " healing " + ally.getLocalName() + " for " + repairment + "hp");
    }

    public boolean canHeal() {
        return canHeal;
    }

    public void setCanHeal(boolean canHeal) {
        this.canHeal = canHeal;
    }
}
