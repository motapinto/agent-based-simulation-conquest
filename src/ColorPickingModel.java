import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ColorPickingModel extends SimModelImpl {
	private ArrayList<ColorPickingAgent> agentList;
	private Schedule schedule;
	private DisplaySurface dsurf;
	private Object2DTorus space;
	private OpenSequenceGraph plot;

	public enum MovingMode { Walk, Jump };

	private int numberOfAgents, spaceSize;
	private MovingMode movingMode;

	private Hashtable<Color, Integer> agentColors;

	public ColorPickingModel() {
		this.numberOfAgents = 100;
		this.spaceSize = 100;
		this.movingMode = MovingMode.Walk;
	}

	public String getName() {
		return "Color Picking Model";
	}

	public String[] getInitParam() {
		return new String[] { "numberOfAgents", "spaceSize", "movingMode"};
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	public int getSpaceSize() {
		return spaceSize;
	}

	public void setSpaceSize(int spaceSize) {
		this.spaceSize = spaceSize;
	}

	public void setMovingMode(MovingMode movingMode) {
		this.movingMode = movingMode;
	}

	public MovingMode getMovingMode() {
		return movingMode;
	}

	public void setup() {
		schedule = new Schedule();
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "Color Picking Display");
		registerDisplaySurface("Color Picking Display", dsurf);

		// property descriptors
		Vector<MovingMode> vMM = new Vector<MovingMode>();
		for(int i=0; i<MovingMode.values().length; i++) {
			vMM.add(MovingMode.values()[i]);
		}
		descriptors.put("MovingMode", new ListPropertyDescriptor("MovingMode", vMM));
	}

	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();
	}

	public void buildModel() {
		agentList = new ArrayList<ColorPickingAgent>();
		space = new Object2DTorus(spaceSize, spaceSize);
		for (int i = 0; i<numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);
			Color color =  new Color(Random.uniform.nextIntFromTo(0,255), Random.uniform.nextIntFromTo(0,255), Random.uniform.nextIntFromTo(0,255));
			ColorPickingAgent agent = new ColorPickingAgent(x, y, color, space);
			space.putObjectAt(x, y, agent);
			agentList.add(agent);
		}
	}

	private void buildDisplay() {
		// space and display surface
		Object2DDisplay display = new Object2DDisplay(space);
		display.setObjectList(agentList);
		dsurf.addDisplayableProbeable(display, "Agents Space");
		dsurf.display();

		// graph
		if (plot != null) plot.dispose();
		plot = new OpenSequenceGraph("Colors and Agents", this);
		plot.setAxisTitles("time", "n");
		// plot number of different existing colors
		plot.addSequence("Number of colors", new Sequence() {
			public double getSValue() {
				return agentColors.size();
			}
		});
		// plot number of agents with the most abundant color
		plot.addSequence("Top color", new Sequence() {
			public double getSValue() {
				int n = 0;
				Enumeration<Integer> agentsPerColor = agentColors.elements();
				while(agentsPerColor.hasMoreElements()) {
					int c = agentsPerColor.nextElement();
					if(c>n) n=c;
				}
				return n;
			}
		});
		plot.display();
	}

	private void buildSchedule() {
		schedule.scheduleActionBeginning(0, new MainAction());
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
	}

	class MainAction extends BasicAction {

		public void execute() {
			// prepare agent colors hashtable
			agentColors = new Hashtable<Color,Integer>();

			// shuffle agents
			SimUtilities.shuffle(agentList);

			// iterate through all agents
			for(int i = 0; i < agentList.size(); i++) {
				ColorPickingAgent agent = (ColorPickingAgent) agentList.get(i);
				if(movingMode == MovingMode.Walk) {
					agent.walk();
				} else {
					agent.jump();
				}
				Color c = agent.recolor();
				int nAgentsWithColor = (agentColors.get(c) == null ? 1 : agentColors.get(c)+1); 
				agentColors.put(c, nAgentsWithColor);
			}
		}

	}


	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new ColorPickingModel(), null, false);
	}

}
