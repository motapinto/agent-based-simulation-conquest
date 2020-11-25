package data;

import java.io.Serializable;

public class Position implements Serializable {
    private final int x;
    private final int y;

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates distance between two positions based on formula d=sqrt((x2−x1)^2+(y2−y1)^2)
     * @param position1
     * @return distance between two positions
     */
    public double calculateDistance(Position position1){
        return Math.sqrt(Math.pow(position1.getX() - this.x, 2) + Math.pow(position1.getY() - this.y, 2));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
