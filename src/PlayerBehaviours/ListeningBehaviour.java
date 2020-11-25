package PlayerBehaviours;

import agents.DirectoryFacilitator;
import agents.Player;
import data.MovementType;
import data.Position;
import data.Team;
import data.message.*;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;

import static data.AgentType.PLAYER;
import static data.MessageType.KILLED;

public class ListeningBehaviour extends CyclicBehaviour {
    private static final long MOVE_TIMEOUT = 500;
    private final Player agent;
    private final static int MAX_HEALTH = 200;
    private final static int MIN_HEALTH = 0;

    public ListeningBehaviour(Player agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = this.agent.receive(DirectoryFacilitator.getMessageTemplate());
        if(msg == null) return;
        AID sender = msg.getSender();

        SimpleMessage simpleMessage;
        try {
            simpleMessage = (SimpleMessage) msg.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return;
        }

        switch (simpleMessage.getMessageType()) {
            /* Zone to Player Messages */
            case POSITION: {
                ZonePositionMessage zonePositionMessage;
                try {
                    zonePositionMessage = (ZonePositionMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                    return;
                }

                AID zonePositionMessageZone = zonePositionMessage.getZone();
                Position pos = zonePositionMessage.getPosition();
                this.agent.getPositionsZones().put(zonePositionMessageZone, pos);

                if(this.agent.getCapturableZones().size() + 2 == this.agent.getPositionsZones().size()) {
                    this.agent.start();
                }

                break;
            }

            case PLAYERS_IN_ZONE: {
                PlayersInZoneMessage playersInZoneMessage;
                try {
                    playersInZoneMessage = (PlayersInZoneMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                    return;
                }

                this.updateTimeout(playersInZoneMessage);

                if (this.agent.getTeam() == Team.ALLIED) {
                    this.agent.setEnemyPlayersInZone(playersInZoneMessage.getAxisAgents());
                    this.agent.setTeamPlayersInZone(playersInZoneMessage.getAlliedAgents());
                    this.agent.getTeamPlayersInZone().remove(this.agent.getAID());
                } else if (this.agent.getTeam() == Team.AXIS) {
                    this.agent.setEnemyPlayersInZone(playersInZoneMessage.getAlliedAgents());
                    this.agent.setTeamPlayersInZone(playersInZoneMessage.getAxisAgents());
                    this.agent.getTeamPlayersInZone().remove(this.agent.getAID());
                }
                break;
            }

            /* Player to Player Messages */
            case ATTACK: case HEAL: {
                PlayerActionMessage playerActionMessage;
                try {
                    playerActionMessage = (PlayerActionMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                    return;
                }

                if(!playerActionMessage.getZone().equals(this.agent.getCurrentZone())) return;
                int value = playerActionMessage.getActionValue();
                this.agent.setHealth(this.agent.getHealth() + value);
                this.agent.getSwingGUIGame().getZoneInformationPanel().addUpdatePlayer(this.agent);
                if(this.agent.getHealth() <= MIN_HEALTH) this.killed();
                break;
            }

            case INFORM_HEALTH: {
                PlayerActionMessage playerActionMessage;
                try {
                    playerActionMessage = (PlayerActionMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                    return;
                }

                int health = playerActionMessage.getActionValue();
                this.agent.getTeamPlayersInZoneHealth().put(sender, health);
                break;
            }

            case BACKUP: {
                PlayerBackupRequestMessage playerBackupRequestMessage;
                try {
                    playerBackupRequestMessage = (PlayerBackupRequestMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                    return;
                }

                AID zone = playerBackupRequestMessage.getZone();
                this.agent.getZonesBackup().put(zone, true);
                break;
            }
        }
    }

    private void updateTimeout(PlayersInZoneMessage message) {
        if(this.agent.getSpawnZone().equals(this.agent.getCurrentZone()))
            return;

        List<AID> teamAgents;
        if (this.agent.getTeam() == Team.ALLIED) {
            teamAgents = message.getAlliedAgents();
        } else {
            teamAgents = message.getAxisAgents();
        }

        if(teamAgents.size() < this.agent.getTeamPlayersInZone().size())
            this.agent.setMoveTimeout(System.currentTimeMillis() + MOVE_TIMEOUT);
    }

    public void killed() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        TeamMessage content = new TeamMessage(PLAYER, KILLED, this.agent.getTeam());

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(this.agent.getGameServerAgent());
        this.agent.send(msg);
        this.agent.setInitialAgentStats();
        this.agent.getSwingGUIGame().getZoneInformationPanel().addPlayerMovingToZone(this.agent.getAID(), this.agent.getCurrentZone(), 0);
        this.agent.getSwingGUIGame().getZoneInformationPanel().addUpdatePlayer(this.agent);
        this.agent.addBehaviour(new MovingBehaviour(this.agent, this.agent.getCurrentZone(), MovementType.LEFT));
        this.agent.addBehaviour(new MovingBehaviour(this.agent, this.agent.getSpawnZone(), MovementType.ENTERED));
        this.agent.logAction(this.agent.getLocalName() + " died, respawning in " + MovingBehaviour.RESPAWN_TIME / 1000 + "s");
    }
}
