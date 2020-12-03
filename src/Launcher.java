import gui.SwingGUIGame;
import gui.SwingGUIStats;
import agents.GameServer;
import agents.Logger;
import agents.Player;
import agents.Zone;
import data.PlayerClass;
import data.Position;
import data.Team;
import data.ZoneType;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.AgentController;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Launcher extends Repast3Launcher {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Launcher.class.getName());
    private String zonesFile = "1.txt", axisPlayersFile = "1.txt", alliedPlayersFile = "1.txt";
    private int initialTickets = 100, gameTime = 100;
    private List<AgentController> agentsList = new ArrayList<>();

    private OpenSequenceGraph plot;

    public static void main(String[] args) {
        SimInit init = new SimInit();
        Launcher l = new Launcher();
        init.loadModel(l, null, false);
    }

    @Override
    public String[] getInitParam() {
        return new String[] {"zonesFile", "axisPlayersFile", "alliedPlayersFile", "initialTickets", "gameTime"};
    }

    @Override
    public String getName() {
        return "aiad-conquest";
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public void setup() {
        super.setup();
        schedule = new Schedule();
    }

    @Override
    public void begin() {
        super.begin();
        buildCharts();
        buildSchedule();
    }

    private void buildCharts() {
        if (plot != null) plot.dispose();
        plot = new OpenSequenceGraph("Colors and Agents", this);
        plot.setAxisTitles("time", "n");

        // plot number of different existing colors
        plot.addSequence("Number of colors", new Sequence() {
            public double getSValue() {
                return 5;
            }
        });

        // plot number of agents with the most abundant color
        plot.addSequence("Top color", new Sequence() {
            public double getSValue() {
                return 10;
            }
        });
        plot.display();
    }

    private void buildSchedule() {
        getSchedule().scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
    }

    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();

        Properties props = new ExtendedProperties();
        props.setProperty("gui", "true");
        props.setProperty("main", (Boolean.TRUE).toString());

        Profile p = new ProfileImpl(props);
        ContainerController container = rt.createMainContainer(p);

        try {
            this.initLogger();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize logger");
            return;
        }

        LOGGER.config("Initializing game-" + 0);
        Logger.setLogger(LOGGER);

        List<Position> zonePositions;
        try {
            zonePositions = parseZones(zonesFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if(zonePositions.size() < 2) {
            System.err.println("Number of zones should be at least 3! The first 2 lines are allied spawn position and axis spawn position");
            return;
        }

        List<PlayerClass> alliedPlayers;
        List<PlayerClass> axisPlayers;

        try {
            alliedPlayers = parsePlayers(alliedPlayersFile);
            axisPlayers = parsePlayers(axisPlayersFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if(alliedPlayers.size() != axisPlayers.size() || alliedPlayers.size() == 0) {
            System.err.println("Number of players should be at least 1!");
            return;
        }

        try {
            launchAgents(container, zonePositions, alliedPlayers, axisPlayers);
        } catch (StaleProxyException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getZonesFile() {
        return zonesFile;
    }

    public void setZonesFile(String zonesFile) {
        this.zonesFile = zonesFile;
    }

    public String getAxisPlayersFile() {
        return axisPlayersFile;
    }

    public void setAxisPlayersFile(String axisPlayersFile) {
        this.axisPlayersFile = axisPlayersFile;
    }

    public String getAlliedPlayersFile() {
        return alliedPlayersFile;
    }

    public void setAlliedPlayersFile(String alliedPlayersFile) {
        this.alliedPlayersFile = alliedPlayersFile;
    }

    public int getInitialTickets() {
        return initialTickets;
    }

    public void setInitialTickets(int initialTickets) {
        this.initialTickets = initialTickets;
    }

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    private void initLogger() throws IOException {
        Handler fileHandler = new FileHandler("./src/logs/game.log");
        Handler consoleHandler = new ConsoleHandler();

        LOGGER.addHandler(fileHandler);
        LOGGER.addHandler(consoleHandler);

        fileHandler.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.SEVERE);
        LOGGER.setLevel(Level.ALL);

        Logger.setLogger(LOGGER);
    }

    private void launchAgents(ContainerController container, List<Position> zonePositions, List<PlayerClass> alliedPlayers, List<PlayerClass> axisPlayers) throws StaleProxyException, FileNotFoundException {
        SwingGUIStats swingGUIStats = new SwingGUIStats();
        Thread threadStats = new Thread(swingGUIStats);
        threadStats.start();

        SwingGUIGame swingGUIGame = new SwingGUIGame(0, initialTickets, gameTime);
        Thread threadGame = new Thread(swingGUIGame);
        threadGame.start();

        agentsList = new ArrayList<>();
        agentsList.add(container.acceptNewAgent("game-server", new GameServer(zonePositions.size() - 2, axisPlayers.size(), initialTickets, gameTime, swingGUIGame, swingGUIStats)));
        agentsList.add(container.acceptNewAgent("allied-spawn", new Zone(zonePositions.get(0), ZoneType.BASE, Team.ALLIED, 0, swingGUIGame, swingGUIStats)));
        agentsList.add(container.acceptNewAgent("axis-spawn", new Zone(zonePositions.get(1), ZoneType.BASE, Team.AXIS, 0, swingGUIGame, swingGUIStats)));

        for (int j = 2; j < zonePositions.size(); j++) {
            char c = (char) ('A' + j - 2);
            agentsList.add(container.acceptNewAgent("zone-" + c, new Zone(zonePositions.get(j), ZoneType.CAPTURABLE, Team.NEUTRAL, 5, swingGUIGame, swingGUIStats)));
        }

        for (int j = 0; j < alliedPlayers.size(); j++) {
            agentsList.add(container.acceptNewAgent("allied-" + j + "-" + alliedPlayers.get(j).toString().toLowerCase(), new Player(Team.ALLIED, alliedPlayers.get(j), swingGUIGame, swingGUIStats)));
        }

        for (int j = 0; j < axisPlayers.size(); j++) {
            agentsList.add(container.acceptNewAgent("axis-" + j + "-" + axisPlayers.get(j).toString().toLowerCase(), new Player(Team.AXIS, axisPlayers.get(j), swingGUIGame, swingGUIStats)));
        }

        for (AgentController agent : agentsList){
            agent.start();
        }
    }

    private static List<PlayerClass> parsePlayers(String agentsFileName) throws FileNotFoundException {
        List<PlayerClass> playerClasses = new ArrayList<>();

        File myObj = new File("players/" + agentsFileName);
        Scanner myReader = new Scanner(myObj);

        for (int i = 1; myReader.hasNextLine(); i++) {
            String line = myReader.nextLine();
            int space = line.indexOf(' ');

            if(space == -1 || space == line.length() - 1) {
                System.err.println("Invalid line in line " + i);
                playerClasses.clear();
                return playerClasses;
            }

            PlayerClass playerClass = PlayerClass.valueOf(line.substring(0, space).toUpperCase());

            int number = Integer.parseInt(line.substring(space + 1));

            for (int j = 0; j < number; j++) {
                playerClasses.add(playerClass);
            }
        }

        myReader.close();
        return playerClasses;
    }

    private static List<Position> parseZones(String zonesFileName) throws FileNotFoundException {
        List<Position> positions = new ArrayList<>();

        File myObj = new File("zones/" + zonesFileName);
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            int x = myReader.nextInt();
            int y = myReader.nextInt();
            positions.add(new Position(x, y));
        }
        myReader.close();

        return positions;
    }
}