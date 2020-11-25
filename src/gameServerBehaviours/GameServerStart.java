package gameServerBehaviours;

import agents.GameServer;
import data.AgentType;
import data.MessageType;
import data.message.SimpleMessage;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionResponder;

import java.io.IOException;

public class GameServerStart extends SimpleBehaviour {
    private final GameServer agent;
    private DFAgentDescription[] alliedPlayers, axisPlayers, zones, alliedSpawn, axisSpawn;

    public GameServerStart(GameServer agent) {
        this.agent = agent;
    }

    public void action() {
        // players : allied-player, axis-player
        // zones: allied-spawn, axis-spawn, zone
        if(alliedPlayers == null || alliedPlayers.length != this.agent.getPlayersPerTeam())
            alliedPlayers = this.agent.searchDF("allied-player");

        if(axisPlayers == null || axisPlayers.length != this.agent.getPlayersPerTeam())
            axisPlayers = this.agent.searchDF("axis-player");

        if(zones == null || zones.length != this.agent.getZoneNumber())
            zones = this.agent.searchDF("zone");

        if(alliedSpawn == null || alliedSpawn.length != 1)
            alliedSpawn = this.agent.searchDF("allied-spawn");
        if(axisSpawn == null || axisSpawn.length != 1)
            axisSpawn = this.agent.searchDF("axis-spawn");
    }

    @Override
    public boolean done() {
        return this.agent.getPlayersPerTeam() == alliedPlayers.length && this.agent.getPlayersPerTeam() == axisPlayers.length &&
            this.agent.getZoneNumber() == zones.length && alliedSpawn.length == 1 && axisSpawn.length == 1 &&
            this.agent.getPlayersPerTeam() * 2 + this.agent.getZoneNumber() + 2 == this.agent.getSubscriptionResponder().getSubscriptions().size();
    }

    @Override
    public int onEnd() {
        ACLMessage gameStart = new ACLMessage(ACLMessage.INFORM);

        try {
            gameStart.setContentObject(new SimpleMessage(AgentType.GAME_SERVER, MessageType.START));
        } catch (IOException e) {
            e.printStackTrace();
            return super.onEnd();
        }

        for (Object subscription : this.agent.getSubscriptionResponder().getSubscriptions()) {
            SubscriptionResponder.Subscription sub = (SubscriptionResponder.Subscription)subscription;
            sub.notify(gameStart);
        }

        this.agent.addBehaviour(new GameServerRun(agent));
        this.agent.removeBehaviour(this);

        this.agent.logConfig("All game configurations done, started game");

        return super.onEnd();
    }
}
