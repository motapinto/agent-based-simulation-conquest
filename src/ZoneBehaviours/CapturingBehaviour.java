package ZoneBehaviours;

import agents.Zone;
import data.MessageType;
import data.Team;
import data.message.PlayerZoneMessage;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionResponder;

import java.io.IOException;

import static data.Team.*;

public class CapturingBehaviour extends TickerBehaviour {

    private final Zone zoneAgent;
    private final static double maxValue = 100;
    private final static double minValue = -100;
    private double lastCapturePoints;


    public CapturingBehaviour(Zone zoneAgent, int period){
        super(zoneAgent, period);
        this.zoneAgent = zoneAgent;
        this.lastCapturePoints = this.zoneAgent.getCapturePoints();
    }

    @Override
    protected void onTick() {
        int numberAlliedPlayers = this.zoneAgent.getAlliedAgents().size();
        int numberAxisPlayers = this.zoneAgent.getAxisAgents().size();
        Team team = this.zoneAgent.getZoneTeam();

        if(this.lastCapturePoints != this.zoneAgent.getCapturePoints()) {
            informSubscribers(null, MessageType.ZONE_POINTS);
            this.lastCapturePoints = this.zoneAgent.getCapturePoints();
        }

        if(numberAlliedPlayers > numberAxisPlayers && team != ALLIED){
            this.zoneAgent.increaseCapturePoints(numberAlliedPlayers - numberAxisPlayers);
            if(this.zoneAgent.getCapturePoints() >= maxValue) {
                this.changeCapturedZone(maxValue, ALLIED);
                this.informSubscribers(ALLIED, MessageType.CAPTURED);
            }
            else if(team == AXIS && this.zoneAgent.getCapturePoints() >= 0){
                this.informSubscribers(NEUTRAL, MessageType.CAPTURED);
                this.changeCapturedZone(0, NEUTRAL);
            }
        } else if(numberAlliedPlayers < numberAxisPlayers && team != Team.AXIS){
            this.zoneAgent.decreaseCapturePoints(numberAxisPlayers - numberAlliedPlayers);
            if(this.zoneAgent.getCapturePoints() <= minValue){
                this.changeCapturedZone(minValue, AXIS);
                this.informSubscribers(AXIS, MessageType.CAPTURED);
            }
            else if(team == ALLIED && this.zoneAgent.getCapturePoints() <= 0){
                this.informSubscribers(NEUTRAL, MessageType.CAPTURED);
                this.changeCapturedZone(0, NEUTRAL);
            }
        } else if(numberAxisPlayers == 0 && numberAlliedPlayers == 0 && !zoneInFinalState()){
            handleZoneInIntermediateState();
        }

        this.zoneAgent.getSwingGUIGame().getZoneMapPanel().addUpdateZone(this.zoneAgent.getAID(), this.zoneAgent.getZoneTeam(),
                this.zoneAgent.getPosition(), this.zoneAgent.getCapturePoints());
    }

    /**
     * Function that handles the zone when this one is in a intermediate state
     */
    private void handleZoneInIntermediateState(){
        switch (this.zoneAgent.getZoneTeam()){
            case ALLIED: {
                this.zoneAgent.increaseCapturePoints(0.2);
                if (this.zoneAgent.getCapturePoints() >= maxValue){
                    this.zoneAgent.setCapturePoints(maxValue);
                }
            }
            break;
            case AXIS: {
                this.zoneAgent.decreaseCapturePoints( 0.2);
                if (this.zoneAgent.getCapturePoints() <= minValue){
                    this.zoneAgent.setCapturePoints(minValue);
                }
            }
            break;

            case NEUTRAL: {
                if(this.zoneAgent.getCapturePoints() > 0) {
                    this.zoneAgent.decreaseCapturePoints(0.2);
                    if (this.zoneAgent.getCapturePoints() <= 0) {
                        this.zoneAgent.setCapturePoints(0);
                    }
                }
                else if(this.zoneAgent.getCapturePoints() < 0) {
                    this.zoneAgent.increaseCapturePoints(0.2);
                    if (this.zoneAgent.getCapturePoints() >= 0) {
                        this.zoneAgent.setCapturePoints(0);
                    }
                }
            }
            break;
        }
    }

    /**
     * Determines if the zone is being captured by one of the teams or is returning to neutral state
     * @return true if it is and false if it isn't
     */
    private Boolean zoneInFinalState(){
        return (this.zoneAgent.getCapturePoints() == maxValue ||
                this.zoneAgent.getCapturePoints() == minValue ||
                this.zoneAgent.getCapturePoints() == 0);
    }

    /**
     * Changes the zone to a captured zone by one of the teams
     * @param newValue New number of capture Points
     * @param zoneTeam Team that captured the zone
     */
    private void changeCapturedZone(double newValue, Team zoneTeam){
        this.zoneAgent.setCapturePoints(newValue);
        this.zoneAgent.setTeam(zoneTeam);
        this.zoneAgent.logAction(this.zoneAgent.getLocalName() + " captured by " + zoneTeam);
    }

    /**
     * Informs all of the zone that it was either captured or it went into neutral
     * @param player Team that captured the zone or null in case the zone is currently neutral
     * @param messageType Captured if a team has captured the zone or neutral if the zone is currently neutral
     */
    private void informSubscribers(Team player, MessageType messageType){
        ACLMessage playerZoneMessage = new ACLMessage(ACLMessage.INFORM);
        try {
            playerZoneMessage.setContentObject(new PlayerZoneMessage(player, messageType, this.zoneAgent.getAID(), this.zoneAgent.getCapturePoints()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (Object subscription : this.zoneAgent.getSubscriptionResponder().getSubscriptions()) {
            SubscriptionResponder.Subscription sub = (SubscriptionResponder.Subscription)subscription;
            sub.notify(playerZoneMessage);
        }
    }

}
