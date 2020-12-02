package data.message;

import data.AgentType;
import data.MessageType;
import data.Team;
import jade.core.AID;

import java.util.Objects;

public class PlayerZoneMessage extends SimpleMessage {
    private final AID zone;
    private final Team team;
    private double currentCapturePoints;

    public PlayerZoneMessage(Team team, MessageType messageType, AID zone, double currentCapturePoints) {
        super(AgentType.ZONE, messageType);
        this.zone = zone;
        this.team = team;
        this.currentCapturePoints = currentCapturePoints;
    }

    public AID getZone() {
        return zone;
    }

    public Team getTeam() {
        return team;
    }

    public double getCurrentCapturePoints() { return currentCapturePoints; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlayerZoneMessage that = (PlayerZoneMessage) o;
        return currentCapturePoints == that.currentCapturePoints &&
                Objects.equals(zone, that.zone) &&
                team == that.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), zone, team, currentCapturePoints);
    }
}