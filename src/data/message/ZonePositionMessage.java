package data.message;

import data.AgentType;
import data.MessageType;
import data.Position;
import jade.core.AID;

import java.util.Objects;

public class ZonePositionMessage extends SimpleMessage {
    private final AID zone;
    private final Position position;

    public ZonePositionMessage(AID zone, Position position) {
        super(AgentType.ZONE, MessageType.POSITION);
        this.zone = zone;
        this.position = position;
    }

    public AID getZone() {
        return zone;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ZonePositionMessage that = (ZonePositionMessage) o;
        return zone == that.zone && position == that.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), zone, position);
    }
}
