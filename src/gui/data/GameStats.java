package gui.data;

import data.Team;

public class GameStats{
    public int alliedPoints;
    public int axisPoints;
    public int gameNumber;
    public Team winner;

    public GameStats(int alliedPoints, int axisPoints, int gameNumber, Team winner) {
        this.alliedPoints = alliedPoints;
        this.axisPoints = axisPoints;
        this.gameNumber = gameNumber;
        this.winner = winner;
    }

    @Override
    public String toString() {
        return "GameNumber: " + this.gameNumber + " Allied Points: " + this.alliedPoints + " Axis Points: " + this.axisPoints + " Winner: " + this.winner;
    }
}