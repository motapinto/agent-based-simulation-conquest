package PlayerBehaviours;

import agents.Player;
import data.MessageType;
import data.Team;
import data.message.PlayerZoneMessage;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

import java.util.Vector;

public class ZoneSubscriber extends SubscriptionInitiator {
    private final Player agent;
    private final static double ALLIED_CAPTURED_POINTS = 100;
    private final static double AXIS_CAPTURED_POINTS = -100;

    public ZoneSubscriber(Player agent) {
        super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));
        this.agent = agent;
    }

    @Override
    protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
        subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        this.agent.getCapturableZones().keySet().forEach(subscription::addReceiver);
        Vector<ACLMessage> v = new Vector<>();
        v.addElement(subscription);
        return v;
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

        AID zone = message.getZone();
        double points = message.getCurrentCapturePoints();
        double prevPoints = this.agent.getCapturableZones().get(zone);

        /** The delta points map save the net gains for each time */
        switch (this.agent.getTeam()) {
            case ALLIED: {
                this.agent.getDeltaZonePoints().put(zone, points - prevPoints);
                break;
            }

            case AXIS: {
                this.agent.getDeltaZonePoints().put(zone, prevPoints - points);
                break;
            }

            default: break;
        }

        switch (message.getMessageType()) {
            case CAPTURED: {
                this.updatePoints(message);

                if(message.getTeam() == Team.ALLIED && points == ALLIED_CAPTURED_POINTS) {
                    this.agent.getCapturableZones().put(zone, ALLIED_CAPTURED_POINTS);
                } else if(message.getTeam() == Team.AXIS && points == AXIS_CAPTURED_POINTS) {
                    this.agent.getCapturableZones().put(zone, AXIS_CAPTURED_POINTS);
                }

                break;
            }

            case NEUTRAL: {
                this.agent.getCapturableZones().put(zone, points);
                break;
            }
        }
    }

    private void updatePoints(PlayerZoneMessage message) {
        if (message.getZone().equals(this.agent.getCurrentZone()) && this.agent.getTeam().equals(message.getTeam()) && message.getMessageType().equals(MessageType.CAPTURED)){
            this.agent.setPoints(this.agent.getPoints() + 100);
            this.agent.getSwingGUIGame().getTeamCompPanel().addUpdateTeamPlayer(this.agent.getTeam(), this.agent.getAID(), this.agent.getPoints(), this.agent.getPlayerClass());
        }
    }
}