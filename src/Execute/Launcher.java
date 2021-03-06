package Execute;

import PlayerBehaviours.HealingBehaviour;
import PlayerBehaviours.MovingBehaviour;
import agents.GameServer;
import agents.Logger;
import agents.Player;
import agents.Zone;
import data.PlayerClass;
import data.Position;
import data.Team;
import data.ZoneType;
import gui.SwingGUIGame;
import gui.SwingGUIStats;
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
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.RectNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Launcher extends Repast3Launcher implements Runnable {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Launcher.class.getName());
    private String MAP = "map1.txt", TEAM_AXIS = "team_1.txt", TEAM_ALLIED = "team_2.txt";
    private int INITIAL_TICKETS = 100, TIME = 100, SPEED_FACTOR = 5;
    private OpenSequenceGraph plot;
    private SwingGUIStats swingGUIStats;
    private SwingGUIGame swingGUIGame;
    private ContainerController container;
    private OpenSequenceGraph zonesCaptured;
    private OpenSequenceGraph playerClassPoints;
    private DisplaySurface dsurf;
    private int WIDTH = 800, HEIGHT = 800;
    private boolean stopSimulation = false;

    private GameServer gameServer;
    private List<Zone> zones;
    private List<Player> alliedPlayers;
    private List<Player> axisPlayers;
    private static List<DefaultDrawableNode> nodes;

    // Independent variables
    public static double MEDIC_ATTACK_FACTOR = 0.6;
    public static double ASSAULT_ATTACK_FACTOR = 1.3;
    public static double SNIPER_ATTACK_FACTOR = 2;
    public static double DEFENDER_ATTACK_FACTOR = 1;



    public Launcher(SwingGUIStats swingGUIStats){
        this.swingGUIStats = swingGUIStats;
    }

    @Override
    public String[] getInitParam() {
        return new String[] {"MAP", "TEAM_AXIS", "TEAM_ALLIED", "INITIAL_TICKETS", "TIME", "SPEED_FACTOR",
            "MEDIC_ATTACK_FACTOR", "ASSAULT_ATTACK_FACTOR", "SNIPER_ATTACK_FACTOR", "DEFENDER_ATTACK_FACTOR",
            "MEDIC_VELOCITY", "ASSAULT_VELOCITY", "SNIPER_VELOCITY", "DEFENDER_VELOCITY",
            "MEDIC_HEALTH", "ASSAULT_HEALTH", "SNIPER_HEALTH", "DEFENDER_HEALTH"
        };
    }

    @Override
    public String getName() {
        return "aiad-conquest";
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void begin() {
        super.begin();
        buildCharts();
        buildDisplaySchedule();
    }

    private void buildCharts() {
        if (plot != null) plot.dispose();
        plot = new OpenSequenceGraph("Team's tickets", this);
        plot.setAxisTitles("time", "tickets");
        plot.addSequence("Allied tickets", () -> gameServer.getTeamTickets().get(0), SwingGUIGame.GREEN, 5);
        plot.addSequence("Axis tickets", () -> gameServer.getTeamTickets().get(1), SwingGUIGame.RED, 5);
        plot.setYRange(0, this.gameServer.getInitialTickets());
        plot.display();

        if (zonesCaptured != null) zonesCaptured.dispose();
        zonesCaptured = new OpenSequenceGraph("Zones Captured", this);
        zonesCaptured.setAxisTitles("time", "n");

        // plot number of different existing colors
        zonesCaptured.addSequence("Axis",() -> zonesPerTeam(Team.AXIS), SwingGUIGame.RED, 5);

        // plot number of agents with the most abundant color
        zonesCaptured.addSequence("Allied", () -> zonesPerTeam(Team.ALLIED), SwingGUIGame.GREEN, 5);

        zonesCaptured.setYRange(0, zones.size());
        zonesCaptured.display();

        if (playerClassPoints != null) playerClassPoints.dispose();
        playerClassPoints = new OpenSequenceGraph("Player class points", this);
        playerClassPoints.setAxisTitles("time", "n");

        playerClassPoints.addSequence("Defender", () -> pointsPerClass(PlayerClass.DEFENDER), SwingGUIGame.RED, 5);

        playerClassPoints.addSequence("Medic", () -> pointsPerClass(PlayerClass.MEDIC), SwingGUIGame.GREEN, 5);

        playerClassPoints.addSequence("Sniper", new Sequence() {
            public double getSValue() {
                return  pointsPerClass(PlayerClass.SNIPER);
            }
        }, new Color(0, 255, 0), 5);

        playerClassPoints.addSequence("Assault", () -> pointsPerClass(PlayerClass.ASSAULT), new Color(255, 255, 0), 5);

        playerClassPoints.setYRange(0, 100);
        playerClassPoints.display();
    }

    private double zonesPerTeam(Team team){
        int counter = 0;
        for(Zone zone: zones){
            if(zone.getZoneTeam().equals(team) && !zone.getZoneType().equals(ZoneType.BASE)){
                counter++;
            }
        }
        return counter;
    }

    private double pointsPerClass(PlayerClass playerClass){
        int counter = 0;
        int numberOfPlayers = 0;
        for(Player player: alliedPlayers){
            if(player.getPlayerClass().equals(playerClass)) {
                numberOfPlayers++;
                counter += player.getPoints();
            }
        }
        for(Player player: axisPlayers){
            if(player.getPlayerClass().equals(playerClass)){
                numberOfPlayers++;
                counter+= player.getPoints();
            }
        }
        if(numberOfPlayers != 0)
            counter /= numberOfPlayers;

        if(counter > playerClassPoints.getYRange()[1])
            playerClassPoints.setYRange(0, counter);

        return counter;
    }

    private void buildDisplaySchedule() {
        // display surface
        if (dsurf != null) dsurf.dispose();
        dsurf = new DisplaySurface(this, "Players Interaction Network Graph");
        registerDisplaySurface("Players Interaction Network Graph", dsurf);
        Network2DDisplay display = new Network2DDisplay(nodes,WIDTH,HEIGHT);
        dsurf.addDisplayableProbeable(display, "Network Display");
        dsurf.addZoomable(display);
        addSimEventListener(dsurf);
        dsurf.display();

        getSchedule().scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
        getSchedule().scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
        getSchedule().scheduleActionAtInterval(1, zonesCaptured, "step", Schedule.LAST);
        getSchedule().scheduleActionAtInterval(1, playerClassPoints, "step", Schedule.LAST);
    }

    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();

        Properties props = new ExtendedProperties();
        props.setProperty("gui", "true");
        props.setProperty("main", (Boolean.TRUE).toString());

        Profile p = new ProfileImpl(props);
        container = rt.createMainContainer(p);

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
            zonePositions = parseZones(MAP);
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
            alliedPlayers = parsePlayers(TEAM_ALLIED);
            axisPlayers = parsePlayers(TEAM_AXIS);
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

    private void launchAgents(ContainerController container, List<Position> zonePositions, List<PlayerClass> alliedPlayersClass, List<PlayerClass> axisPlayersClass) throws StaleProxyException, FileNotFoundException {

        if(swingGUIGame != null) {
            swingGUIGame.closeSwingGUI();
        }

        nodes = new ArrayList<>();

        swingGUIGame = new SwingGUIGame(0, INITIAL_TICKETS, TIME);
        Thread threadGame = new Thread(swingGUIGame);
        threadGame.start();

        List<AgentController> agentsList = new ArrayList<>();
        gameServer =  new GameServer(zonePositions.size() - 2, axisPlayersClass.size(), INITIAL_TICKETS, TIME, swingGUIGame, swingGUIStats, SPEED_FACTOR);
        agentsList.add(container.acceptNewAgent("game-server", gameServer));

        zones = new ArrayList<>();
        zones.add(new Zone(zonePositions.get(0), ZoneType.BASE, Team.ALLIED, 0, swingGUIGame, swingGUIStats, SPEED_FACTOR));
        zones.add(new Zone(zonePositions.get(1), ZoneType.BASE, Team.AXIS, 0, swingGUIGame, swingGUIStats, SPEED_FACTOR));
        agentsList.add(container.acceptNewAgent("allied-spawn", zones.get(0)));
        agentsList.add(container.acceptNewAgent("axis-spawn", zones.get(1)));

        for (int j = 2; j < zonePositions.size(); j++) {
            char c = (char) ('A' + j - 2);
            zones.add(new Zone(zonePositions.get(j), ZoneType.CAPTURABLE, Team.NEUTRAL, 5, swingGUIGame, swingGUIStats, SPEED_FACTOR));
            agentsList.add(container.acceptNewAgent("zone-" + c, zones.get(j)));
        }

        alliedPlayers = new ArrayList<>();
        for (int j = 0; j < alliedPlayersClass.size(); j++) {
            Player alliedPlayer = new Player(Team.ALLIED, alliedPlayersClass.get(j), swingGUIGame, swingGUIStats, SPEED_FACTOR);
            DefaultDrawableNode node =
                    generateNode("allied-" + j + "-" + alliedPlayersClass.get(j).toString().toLowerCase(), SwingGUIGame.GREEN, (int) (90 + Math.random() * 30), (HEIGHT/alliedPlayersClass.size())*j);
            nodes.add(node);
            alliedPlayer.setNode(node);
            alliedPlayers.add(alliedPlayer);
            agentsList.add(container.acceptNewAgent("allied-" + j + "-" + alliedPlayersClass.get(j).toString().toLowerCase(), alliedPlayers.get(j)));
        }

        axisPlayers = new ArrayList<>();
        for (int j = 0; j < axisPlayersClass.size(); j++) {
            Player axisPlayer = new Player(Team.AXIS, axisPlayersClass.get(j), swingGUIGame, swingGUIStats, SPEED_FACTOR);
            DefaultDrawableNode node =
                    generateNode("axis-" + j + "-" + axisPlayersClass.get(j).toString().toLowerCase(), SwingGUIGame.RED, (int) (WIDTH- 190 - Math.random() * 30), (HEIGHT/alliedPlayersClass.size())*j);
            nodes.add(node);
            axisPlayer.setNode(node);
            axisPlayers.add(axisPlayer);
            agentsList.add(container.acceptNewAgent("axis-" + j + "-" + axisPlayersClass.get(j).toString().toLowerCase(), axisPlayer));
        }

        for (AgentController agent : agentsList) {
            agent.start();
        }
    }

    private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
        RectNetworkItem oval = new RectNetworkItem(x,y);
        oval.allowResizing(true);
        oval.setHeight(50);
        oval.setWidth(120);

        DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
        node.setColor(color);
        node.setLabelColor(Color.BLACK);
        node.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        return node;
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

    @Override
    public void stopSimulation() {
        this.stopSimulation = true;
        swingGUIGame.closeSwingGUI();
        super.stopSimulation();
    }

    public static DefaultDrawableNode getNode(String label) {
        for(DefaultDrawableNode node : nodes) {
            if(node.getNodeLabel().equals(label)) {
                return node;
            }
        }
        return null;
    }

    public String getMAP() {
        return this.MAP;
    }

    public void setMAP(String MAP) {
        this.MAP = MAP;
    }

    public String getTEAM_AXIS() {
        return TEAM_AXIS;
    }

    public void setTEAM_AXIS(String TEAM_AXIS) {
        this.TEAM_AXIS = TEAM_AXIS;
    }

    public String getTEAM_ALLIED() {
        return TEAM_ALLIED;
    }

    public void setTEAM_ALLIED(String TEAM_ALLIED) {
        this.TEAM_ALLIED = TEAM_ALLIED;
    }

    public int getINITIAL_TICKETS() {
        return INITIAL_TICKETS;
    }

    public void setINITIAL_TICKETS(int INITIAL_TICKETS) {
        this.INITIAL_TICKETS = INITIAL_TICKETS;
    }

    public int getTIME() {
        return TIME;
    }

    public void setTIME(int TIME) {
        this.TIME = TIME;
    }

    public int getSPEED_FACTOR() {
        return SPEED_FACTOR;
    }

    public void setSPEED_FACTOR(int SPEED_FACTOR) {
        this.SPEED_FACTOR = SPEED_FACTOR;
    }

    // Most important independent variables below
    public double getMEDIC_ATTACK_FACTOR() {
        return MEDIC_ATTACK_FACTOR;
    }

    public void setMEDIC_ATTACK_FACTOR(double MEDIC_ATTACK_FACTOR) {
        Launcher.MEDIC_ATTACK_FACTOR = MEDIC_ATTACK_FACTOR;
    }

    public double getASSAULT_ATTACK_FACTOR() {
        return ASSAULT_ATTACK_FACTOR;
    }

    public boolean isStopSimulation() { return stopSimulation; }

    public GameServer getGameServer() { return gameServer; }

    public void setASSAULT_ATTACK_FACTOR(double ASSAULT_ATTACK_FACTOR) {
        Launcher.ASSAULT_ATTACK_FACTOR = ASSAULT_ATTACK_FACTOR;
    }

    public double getSNIPER_ATTACK_FACTOR() {
        return SNIPER_ATTACK_FACTOR;
    }

    public void setSNIPER_ATTACK_FACTOR(double SNIPER_ATTACK_FACTOR) {
        Launcher.SNIPER_ATTACK_FACTOR = SNIPER_ATTACK_FACTOR;
    }

    public double getDEFENDER_ATTACK_FACTOR() {
        return DEFENDER_ATTACK_FACTOR;
    }

    public void setDEFENDER_ATTACK_FACTOR(double DEFENDER_ATTACK_FACTOR) {
        Launcher.DEFENDER_ATTACK_FACTOR = DEFENDER_ATTACK_FACTOR;
    }

    public double getMEDIC_VELOCITY() {
        return MovingBehaviour.MEDIC_VELOCITY;
    }

    public void setMEDIC_VELOCITY(double MEDIC_VELOCITY) {
        MovingBehaviour.MEDIC_VELOCITY = MEDIC_VELOCITY;
    }

    public double getASSAULT_VELOCITY() {
        return MovingBehaviour.ASSAULT_VELOCITY;
    }

    public void setASSAULT_VELOCITY(double ASSAULT_VELOCITY) {
        MovingBehaviour.ASSAULT_VELOCITY = ASSAULT_VELOCITY;
    }

    public double getSNIPER_VELOCITY() {
        return MovingBehaviour.SNIPER_VELOCITY;
    }

    public void setSNIPER_VELOCITY(double SNIPER_VELOCITY) {
        MovingBehaviour.SNIPER_VELOCITY = SNIPER_VELOCITY;
    }

    public double getDEFENDER_VELOCITY() {
        return MovingBehaviour.DEFENDER_VELOCITY;
    }

    public void setDEFENDER_VELOCITY(double DEFENDER_VELOCITY) {
        MovingBehaviour.DEFENDER_VELOCITY = DEFENDER_VELOCITY;
    }

    public int getMEDIC_HEALTH() {
        return HealingBehaviour.MEDIC_HEALTH;
    }

    public void setMEDIC_HEALTH(int MEDIC_HEALTH) {
        HealingBehaviour.MEDIC_HEALTH = MEDIC_HEALTH;
    }

    public int getASSAULT_HEALTH() {
        return HealingBehaviour.ASSAULT_HEALTH;
    }

    public void setASSAULT_HEALTH(int ASSAULT_HEALTH) {
        HealingBehaviour.ASSAULT_HEALTH = ASSAULT_HEALTH;
    }

    public int getSNIPER_HEALTH() {
        return HealingBehaviour.SNIPER_HEALTH;
    }

    public void setSNIPER_HEALTH(int SNIPER_HEALTH) {
        HealingBehaviour.SNIPER_HEALTH = SNIPER_HEALTH;
    }

    public int getDEFENDER_HEALTH() {
        return HealingBehaviour.DEFENDER_HEALTH;
    }

    public void setDEFENDER_HEALTH(int DEFENDER_HEALTH) {
        HealingBehaviour.DEFENDER_HEALTH = DEFENDER_HEALTH;
    }

    @Override
    public void run() {
        SimInit init = new SimInit();
        init.loadModel(this, null, false);
    }
}