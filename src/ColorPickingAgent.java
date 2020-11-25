import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.*;
import java.util.Vector;

public class ColorPickingAgent implements Drawable {
	private int x, y;
	private Color color;
	private Object2DTorus space;

	public ColorPickingAgent(int x, int y, Color color, Object2DTorus space){
		this.x = x;
		this.y = y;
		this.color = color;
		this.space = space;
	}

	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
	}

	public void jump() {
		space.putObjectAt(this.x, this.y,null);
		do {
			this.x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
			this.y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
		} while (space.getObjectAt(x, y) != null);
		space.putObjectAt(x, y, this);
	}

	public void walk() {
		int xMove = Random.uniform.nextIntFromTo(0, 2)-1;
		int yMove = Random.uniform.nextIntFromTo(0, 2)-1;
		if(space.getObjectAt(this.x+xMove, this.y+yMove) == null) {
			space.putObjectAt(this.x, this.y,null);
			this.x += xMove;
			this.y += yMove;
			space.putObjectAt(this.x, this.y, this);
		}
	}

	// select random neighbor and take his color if different
	public Color recolor() {
		Vector v = space.getMooreNeighbors(x, y, false);
		if(v.size()>0) {
			this.color = ((ColorPickingAgent) v.get(Random.uniform.nextIntFromTo(0, v.size()-1))).getColor();
		}
		return this.color;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Color getColor() {
		return color;
	}

}
