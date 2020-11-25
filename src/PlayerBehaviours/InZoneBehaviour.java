package PlayerBehaviours;

import agents.Player;
import data.MovementType;
import data.Position;
import data.Team;
import data.message.PlayerBackupRequestMessage;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.*;

import static data.PlayerClass.DEFENDER;
import static data.PlayerClass.MEDIC;

public class InZoneBehaviour extends TickerBehaviour {
    private final Player agent;
    private int playerHealth;

    private HealingBehaviour healingBehaviour;
    private AttackingBehaviour attackingBehaviour;

    private static final int MAX_HEALTH = 200;

    private static final int VALUE_OF_SMALL_DELTA = 10;
    private static final int ALLIED_CAPTURED_POINTS = 100;
    private static final int AXIS_CAPTURED_POINTS = -100;
    private static final int NEUTRAL_POINTS = 0;

    public InZoneBehaviour(Player agent, long period) {
        super(agent, period);
        this.agent = agent;
        this.playerHealth = 0;
        this.healingBehaviour = new HealingBehaviour(this.agent);
        this.attackingBehaviour = new AttackingBehaviour(this.agent);

        for(AID teamPlayer : this.agent.getTeamPlayersInZone()) {
            this.agent.getTeamPlayersInZoneHealth().put(teamPlayer, MAX_HEALTH);
        }
    }

    /**
     * Decides either to call for backup, to attack, to heal other players or to move to other zone
     */
    @Override
    protected void onTick() {
        List<AID> teamPlayersInZone = this.agent.getTeamPlayersInZone();
        List<AID> enemiesInZone = this.agent.getEnemyPlayersInZone();
        AID bestZone = this.getBestZone();
        AID currentZone = this.agent.getCurrentZone();

        /** Should stay on the current zone */
        if(bestZone.equals(currentZone)) {
            Random rand = new Random();

            /** Informs team players if health has changed */
            if(this.agent.getHealth() != this.playerHealth) {
                this.agent.addBehaviour(new InformHealthBehaviour(this.agent));
                this.playerHealth = this.agent.getHealth();
            }

            /** Request for Backup */
            if(teamPlayersInZone.size() < enemiesInZone.size() || this.agent.getDeltaZonePoints().get(currentZone) <= 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                PlayerBackupRequestMessage content = new PlayerBackupRequestMessage(currentZone);

                try {
                    msg.setContentObject(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.agent.sendMessageToTeamMembersNotInZone(msg);
            }

            /** Attack a random enemy player */
            if(this.attackingBehaviour.canAttack() && enemiesInZone.size() != 0) {
                int index = rand.nextInt(enemiesInZone.size());
                AID selectedEnemy = enemiesInZone.get(index);

                this.attackingBehaviour = new AttackingBehaviour(this.agent, selectedEnemy);
                this.agent.addBehaviour(this.attackingBehaviour);
            }

            /** Heal the team player with the least health */
            else if(this.agent.getPlayerClass() == MEDIC && teamPlayersInZone.size() != 0 &&
                    (this.healingBehaviour == null || this.healingBehaviour.canHeal())) {

                this.agent.getTeamPlayersInZoneHealth().put(this.agent.getAID(), Player.getMaxHealth(this.agent.getPlayerClass()) - this.agent.getHealth());
                AID selectedAlly = Collections.max(this.agent.getTeamPlayersInZoneHealth().entrySet(),
                        Comparator.comparingInt(Map.Entry::getValue)).getKey();

                if (selectedAlly.equals(this.agent.getAID()) && this.agent.getHealth() == Player.getMaxHealth(this.agent.getPlayerClass())) {
                    return;
                }

                this.healingBehaviour = new HealingBehaviour(this.agent, selectedAlly);
                this.agent.addBehaviour(this.healingBehaviour);
            }

        /** Should move to other zone */
        } else {
            this.agent.addBehaviour(new MovingBehaviour(this.agent, this.agent.getCurrentZone(), MovementType.LEFT));
            this.agent.addBehaviour(new MovingBehaviour(this.agent, bestZone, MovementType.ENTERED));
        }
    }

    /**
     * Determines best zone based on:
     * * the distance to the zone
     * * requests for backup
     * * points of the zone
     * * points variation/delta since the last tick
     */
    private AID getBestZone() {
        Map<AID, Double> zonesUtility = new HashMap<>();

        this.agent.getCapturableZones().forEach((zone, zonePoints) -> {
            if(zone == this.agent.getSpawnZone()) {
                zonesUtility.put(zone, Double.MIN_VALUE);
                return;
            }

            if (zone == this.agent.getCurrentZone()) {
                if(this.agent.getEnemyPlayersInZone().size() > 0 || zonePoints < 100 && this.agent.getTeam() == Team.ALLIED
                    || zonePoints > -100 && this.agent.getTeam() == Team.AXIS)
                    zonesUtility.put(zone, Double.MAX_VALUE);
                else
                    zonesUtility.put(zone, this.agent.getPlayerClass() == DEFENDER ? this.valueOfFriendZone() : 0.0);
                return;
            }

            if (this.agent.getMoveTimeout() > System.currentTimeMillis()) {
                zonesUtility.put(zone, Double.MIN_VALUE);
                return;
            }

            Position currentZonePosition = this.agent.getPositionsZones().get(this.agent.getCurrentZone());
            double distanceUtility = - (this.agent.getPositionsZones().get(zone).calculateDistance(currentZonePosition) / 10);
            double backupUtility = this.agent.getZonesBackup().get(zone) ? this.valueOfBackup() : 0;
            double deltaPointsUtility = - Math.pow(this.agent.getDeltaZonePoints().get(zone), 2) + VALUE_OF_SMALL_DELTA;

            double zoneUtility = distanceUtility + backupUtility;

            if (zonePoints == NEUTRAL_POINTS) {
                zoneUtility += this.valueOfNeutralZone();
            } else if (zonePoints == AXIS_CAPTURED_POINTS && this.agent.getTeam() == Team.AXIS ||
                zonePoints == ALLIED_CAPTURED_POINTS && this.agent.getTeam() == Team.ALLIED) {
                zoneUtility += this.valueOfFriendZone();
            } else if (zonePoints == AXIS_CAPTURED_POINTS && this.agent.getTeam() == Team.ALLIED ||
                    zonePoints == ALLIED_CAPTURED_POINTS && this.agent.getTeam() == Team.AXIS) {
                zoneUtility += this.valueOfEnemyZone();
            } else if (this.agent.getTeam() == Team.AXIS) {
                zoneUtility += deltaPointsUtility;
                zoneUtility += Double.max(zonePoints / AXIS_CAPTURED_POINTS * this.valueOfFriendZone(), 0);
                zoneUtility += Double.max(zonePoints / ALLIED_CAPTURED_POINTS * this.valueOfEnemyZone(), 0);
            } else {
                zoneUtility += deltaPointsUtility;
                zoneUtility += Double.max(zonePoints / AXIS_CAPTURED_POINTS * this.valueOfEnemyZone(), 0);
                zoneUtility += Double.max(zonePoints / ALLIED_CAPTURED_POINTS * this.valueOfFriendZone(), 0);
            }

            zonesUtility.put(zone, zoneUtility);
        });


        return Collections.max(zonesUtility.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
    }

    private double valueOfBackup() {
        switch (this.agent.getPlayerClass()) {
            case DEFENDER: return 0;
            case SNIPER: case ASSAULT: case MEDIC: default: return 35;
        }
    }

    private double valueOfEnemyZone() {
        switch (this.agent.getPlayerClass()) {
            case DEFENDER: return 10;
            case SNIPER: case ASSAULT: case MEDIC: default: return 30;
        }
    }

    private double valueOfNeutralZone() {
        return 50;
    }

    private double valueOfFriendZone() {
        switch (this.agent.getPlayerClass()) {
            case DEFENDER: return 30;
            case SNIPER: case ASSAULT: case MEDIC: default: return 10;
        }
    }
}


