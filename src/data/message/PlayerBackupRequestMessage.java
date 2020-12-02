package data.message;

import data.AgentType;
import data.MessageType;
import jade.core.AID;

import java.util.Objects;

public class PlayerBackupRequestMessage extends SimpleMessage {
    private final AID zone;

    public PlayerBackupRequestMessage(AID zone) {
        super(AgentType.PLAYER, MessageType.BACKUP);
        this.zone = zone;
    }

    public AID getZone() {
        return zone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlayerBackupRequestMessage that = (PlayerBackupRequestMessage) o;
        return zone == that.zone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), zone);
    }
}