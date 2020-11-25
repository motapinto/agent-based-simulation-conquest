package data.message;

import data.AgentType;
import data.MessageType;
import data.Team;

import java.util.Objects;

public class TeamMessage extends SimpleMessage {
    private Team team;

    public TeamMessage(AgentType agentType, MessageType messageType, Team team) {
        super(agentType, messageType);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamMessage that = (TeamMessage) o;
        return team == that.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), team);
    }
}
