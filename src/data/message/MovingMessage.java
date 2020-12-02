package data.message;

import data.AgentType;
import data.MessageType;
import data.MovementType;
import data.Team;
import jade.core.AID;

import java.util.Objects;

public class MovingMessage extends TeamMessage {
    private final MovementType movementType;

    public MovingMessage(AgentType agentType, Team team, MovementType movementType, AID zone) {
        super(agentType, MessageType.MOVING, team);
        this.movementType = movementType;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MovingMessage that = (MovingMessage) o;
        return movementType == that.movementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), movementType);
    }
}
