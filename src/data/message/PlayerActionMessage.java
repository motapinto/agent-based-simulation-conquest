package data.message;

import data.AgentType;
import data.MessageType;
import jade.core.AID;

import java.util.Objects;

public class PlayerActionMessage extends SimpleMessage {
    private final int actionValue;
    private final AID zone;

    public PlayerActionMessage(AgentType agentType, MessageType messageType, AID zone, int actionValue) {
        super(agentType, messageType);
        this.zone = zone;
        this.actionValue = actionValue;
    }

    public AID getZone() {
        return zone;
    }

    public int getActionValue() {
        return actionValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlayerActionMessage that = (PlayerActionMessage) o;
        return zone == that.zone && actionValue == that.actionValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), zone, actionValue);
    }
}