package ZoneBehaviours;
import agents.Zone;
import data.message.PlayersInZoneMessage;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class InformPlayersInZoneBehaviour extends OneShotBehaviour {

    private final Zone zoneAgent;

    public InformPlayersInZoneBehaviour(Zone zoneAgent){
        super(zoneAgent);
        this.zoneAgent = zoneAgent;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(new PlayersInZoneMessage(this.zoneAgent.getAlliedAgents(), this.zoneAgent.getAxisAgents()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.zoneAgent.getAlliedAgents().forEach(msg::addReceiver);
        this.zoneAgent.getAxisAgents().forEach(msg::addReceiver);
        this.zoneAgent.send(msg);
    }

}