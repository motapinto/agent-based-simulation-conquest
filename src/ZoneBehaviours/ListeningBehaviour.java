package ZoneBehaviours;
import agents.DirectoryFacilitator;
import agents.Zone;
import data.message.MovingMessage;
import data.message.SimpleMessage;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;


public class ListeningBehaviour extends CyclicBehaviour {

    private final Zone agent;
    public ListeningBehaviour(Zone agent) {
        super();
        this.agent = agent;
    }

    @Override
    public void action() {
        MessageTemplate pattern = DirectoryFacilitator.getMessageTemplate();

        ACLMessage msg = this.agent.receive(pattern);
        if(msg == null) return;

        AID sender = msg.getSender();

        if(this.agent.getPlayerAgents().contains(sender)){
            try {
                SimpleMessage simpleMessage = (SimpleMessage) msg.getContentObject();
                switch (simpleMessage.getMessageType()) {
                    case MOVING:
                        handleMoving(msg);
                        this.agent.addBehaviour(new InformPlayersInZoneBehaviour(this.agent));
                    break;
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Determines which player of a certain team entered/left the zone and increases/decreases the number of agents of that
     * team in that zone
     * @param aclMessage ACLMessage received from a player Agent
     * @throws UnreadableException throws if the message content Object is impossible to read
     */
    private void handleMoving(ACLMessage aclMessage) throws UnreadableException {
        MovingMessage movingMessage = (MovingMessage) aclMessage.getContentObject();
        switch (movingMessage.getMovementType()) {
            case ENTERED:
                this.agent.playerEnteredZone(movingMessage.getTeam(), aclMessage.getSender());
                this.agent.logAction(aclMessage.getSender().getLocalName() + " entered " + this.agent.getLocalName());
                break;
            case LEFT:
                this.agent.playerLeftZone(movingMessage.getTeam(), aclMessage.getSender());
                this.agent.logAction(aclMessage.getSender().getLocalName() + " left " + this.agent.getLocalName());
                break;
        }
    }

    /**
     * Sends a reply message to an agent
     * @param message Message to be sent
     * @param aclMessage ACLMessage received from an agent
     */
    private void sendReply(SimpleMessage message, ACLMessage aclMessage){
        ACLMessage zonePosition = aclMessage.createReply();
        zonePosition.setPerformative(ACLMessage.INFORM);
        try {
            zonePosition.setContentObject(message);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.agent.send(zonePosition);
    }
}