package ZoneBehaviours;
import agents.Zone;
import data.message.ZonePositionMessage;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class InformPositionBehaviour extends OneShotBehaviour {
    private final Zone zoneAgent;

    public InformPositionBehaviour(Zone zoneAgent){
        super(zoneAgent);
        this.zoneAgent = zoneAgent;
    }

    @Override
    public void action() {

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(new ZonePositionMessage(this.zoneAgent.getAID(), this.zoneAgent.getPosition()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.zoneAgent.getPlayerAgents().forEach(msg::addReceiver);
        this.zoneAgent.send(msg);
    }
}
