package data.message;


import data.AgentType;
import data.MessageType;

import java.io.Serializable;
import java.util.Objects;

public class SimpleMessage implements Serializable {
    private final AgentType agentType;
    private final MessageType messageType;

    public SimpleMessage(AgentType agentType, MessageType messageType) {
        this.agentType = agentType;
        this.messageType = messageType;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMessage that = (SimpleMessage) o;
        return agentType == that.agentType && messageType == that.messageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentType, messageType);
    }
}
