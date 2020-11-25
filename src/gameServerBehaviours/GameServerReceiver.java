package gameServerBehaviours;

import agents.GameServer;
import data.MessageType;
import data.Team;
import data.message.PlayerZoneMessage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

import java.util.Vector;

public class GameServerReceiver extends SubscriptionInitiator {
     private final GameServer agent;

    public GameServerReceiver(GameServer a, ACLMessage msg){
        super(a, msg);
        this.agent = a;
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        PlayerZoneMessage message;
        try {
            message = (PlayerZoneMessage) inform.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return;
        }

        if(message.getMessageType() == MessageType.CAPTURED) {
            this.agent.getZones().put(message.getZone(), message.getTeam());
        } else if(message.getMessageType() == MessageType.NEUTRAL) {
            this.agent.getZones().put(message.getZone(), Team.NEUTRAL);
        }
    }

    @Override
    protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
        subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        Vector<ACLMessage> vector = new Vector<>();

        DFAgentDescription[] result = this.agent.searchDF("zone");
        for (DFAgentDescription dfAgentDescription : result) {
            subscription.addReceiver(dfAgentDescription.getName());
        }

        vector.add(subscription);
        return vector;
    }
}
