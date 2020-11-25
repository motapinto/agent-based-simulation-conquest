package SharedBehaviours;

import agents.DirectoryFacilitator;
import data.AgentType;
import data.message.SimpleMessage;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

import java.util.Vector;

public class GameServerSubscriber extends SubscriptionInitiator {
    private final DirectoryFacilitator agent;
    private final AID gameServerAgent;

    public GameServerSubscriber(DirectoryFacilitator agent, AID gameServerAgent) {
        super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));
        this.agent = agent;
        this.gameServerAgent = gameServerAgent;
    }

    @Override
    protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
        subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        subscription.addReceiver(this.gameServerAgent);
        Vector<ACLMessage> v = new Vector<>();
        v.addElement(subscription);
        return v;
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        SimpleMessage message;
        try {
            message = (SimpleMessage) inform.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return;
        }

        if(message.getAgentType() == AgentType.GAME_SERVER){
            switch (message.getMessageType()){
                case START: this.agent.init(); break;
                case GAME_OVER: this.agent.end(); break;
            }
        }
    }
}
