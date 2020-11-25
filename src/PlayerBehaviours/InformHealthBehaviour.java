package PlayerBehaviours;

import agents.Player;
import data.message.PlayerActionMessage;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

import static data.AgentType.PLAYER;
import static data.MessageType.INFORM_HEALTH;

public class InformHealthBehaviour extends OneShotBehaviour {
    private final Player agent;

    public InformHealthBehaviour(Player agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        PlayerActionMessage content = new PlayerActionMessage(PLAYER, INFORM_HEALTH, this.agent.getCurrentZone(), Player.getMaxHealth(this.agent.getPlayerClass()) - this.agent.getHealth());

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.agent.sendMessageToTeamMembersInZone(msg);
        this.agent.send(msg);
    }
}
