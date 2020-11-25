package gameServerBehaviours;

import agents.DirectoryFacilitator;
import agents.GameServer;
import data.AgentType;
import data.MessageType;
import data.Team;
import data.message.SimpleMessage;
import data.message.TeamMessage;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionResponder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class GameServerRun extends TickerBehaviour {
    private static final int drainageTick = 2000; // time (ms) before the ticket score is changed
    private final long timeEnd;
    private final GameServer agent;
    private final ArrayList<Integer> teamTickets;

    public GameServerRun(GameServer agent) {
        super(agent, drainageTick);

        this.agent = agent;
        this.teamTickets = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            this.teamTickets.add(this.agent.getInitialTickets());
        }

        this.agent.getSwingGUIGame().startGame();
        this.timeEnd = System.currentTimeMillis() + this.agent.getGameTime() * 1000L;
        this.agent.logAction("Game started. Allied: " + this.teamTickets.get(0) + " | Axis: " + this.teamTickets.get(1));
    }

    @Override
    protected void onTick() {

        if(this.agent.isFinished()) {
            return;
        }

        boolean updated = false;

        ArrayList<Integer> teamCaptures = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            teamCaptures.add(0);
        }

        for (Map.Entry<AID, Team> entry : this.agent.getZones().entrySet()) {
            int team = entry.getValue().ordinal();
            teamCaptures.set(team, teamCaptures.get(team) + 1);
        }

        int bestTeam = this.bestTeam(teamCaptures);

        for (int i = 1; i < teamCaptures.size(); i++) {
            if (i == bestTeam) {
                continue;
            }

            int drainRate = this.drainRate(teamCaptures.get(bestTeam) - teamCaptures.get(i));
            updated = !teamCaptures.get(bestTeam).equals(teamCaptures.get(i));
            this.teamTickets.set(i - 1, this.teamTickets.get(i - 1) - drainRate);
        }

        // Check if tickets were updated
        if(updated || this.receiveDeathMessages()) {
            this.agent.logAction("Tickets updated. Allied: " + this.teamTickets.get(0) + " | Axis: " + this.teamTickets.get(1));
            this.agent.getSwingGUIGame().getGameTimeAndPointsPanel().setAlliedPoints(this.teamTickets.get(0));
            this.agent.getSwingGUIGame().getGameTimeAndPointsPanel().setAxisPoints(this.teamTickets.get(1));
        }

        this.verifyEnd();
    }

    private boolean receiveDeathMessages() {
        boolean updated = false;
        ACLMessage msg;
        MessageTemplate mt = DirectoryFacilitator.getMessageTemplate();

        while ((msg = this.agent.receive(mt)) != null) {
            TeamMessage message;

            try {
                message = (TeamMessage) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
                return updated;
            }

            if(message.getAgentType() != AgentType.PLAYER || message.getMessageType() != MessageType.KILLED) {
                continue;
            }

            int team = message.getTeam().ordinal() - 1;
            this.teamTickets.set(team, this.teamTickets.get(team) - 1);
            updated = true;
        }

        return updated;
    }

    private void verifyEnd() {
        for (int tickets : this.teamTickets) {
            if(tickets <= 0) {
                this.initiateStop();
            }
        }

        // time exceeded
        if(System.currentTimeMillis() >= this.timeEnd) {
            this.initiateStop();
        }
    }

    private void initiateStop() {
        ACLMessage gameEnd = new ACLMessage(ACLMessage.INFORM);

        try {
            gameEnd.setContentObject(new SimpleMessage(AgentType.GAME_SERVER, MessageType.GAME_OVER));
        } catch (IOException e) {
            e.printStackTrace();
            this.stop();
            return;
        }

        for (Object subscription : this.agent.getSubscriptionResponder().getSubscriptions()) {
            SubscriptionResponder.Subscription sub = (SubscriptionResponder.Subscription)subscription;
            sub.notify(gameEnd);
        }

        this.updateStatsGui();
        this.agent.removeBehaviour(this);
        this.agent.logConfig("Game ended, suspending");
        this.agent.setFinished(true);
    }

    private void updateStatsGui(){
        if(this.teamTickets.get(0) > this.teamTickets.get(1))
            this.agent.getSwingGUIStats().addNewGame(this.teamTickets.get(0), this.teamTickets.get(1), this.agent.getSwingGUIGame().getGameNumber(), Team.ALLIED);
        else if(this.teamTickets.get(0) < this.teamTickets.get(1))
            this.agent.getSwingGUIStats().addNewGame(this.teamTickets.get(0), this.teamTickets.get(1), this.agent.getSwingGUIGame().getGameNumber(), Team.AXIS);
        else
            this.agent.getSwingGUIStats().addNewGame(this.teamTickets.get(0), this.teamTickets.get(1), this.agent.getSwingGUIGame().getGameNumber(), Team.NEUTRAL);
    }

    private int bestTeam(ArrayList<Integer> teamCaptures) {
        int bestTeam = 0, mostCaptures = 0;

        for (int i = 1; i < teamCaptures.size(); i++) {

            if(mostCaptures < teamCaptures.get(i)) {
                mostCaptures = teamCaptures.get(i);
                bestTeam = i;
            }
        }

        return bestTeam;
    }

    private int drainRate(int difference) {
        if(difference == this.agent.getZoneNumber()) return this.agent.getZoneNumber() * 2; //Penalize loosing all flags
        return difference;
    }
}
