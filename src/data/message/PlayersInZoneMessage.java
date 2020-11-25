package data.message;
import data.AgentType;
import jade.core.AID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static data.MessageType.PLAYERS_IN_ZONE;

public class PlayersInZoneMessage extends SimpleMessage {
    private final ArrayList<AID> alliedAgents;
    private final ArrayList<AID> axisAgents;

    public PlayersInZoneMessage(ArrayList<AID> alliedAgents, ArrayList<AID> axisAgents) {
        super(AgentType.ZONE, PLAYERS_IN_ZONE);
        this.alliedAgents = new ArrayList<>(alliedAgents);
        this.axisAgents= new ArrayList<>(axisAgents);
    }

    public List<AID> getAlliedAgents() { return alliedAgents; }

    public List<AID> getAxisAgents() { return axisAgents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlayersInZoneMessage that = (PlayersInZoneMessage) o;
        return Objects.equals(alliedAgents, that.alliedAgents) &&
                Objects.equals(axisAgents, that.axisAgents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), alliedAgents, axisAgents);
    }
}
