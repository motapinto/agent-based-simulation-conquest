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
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

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

public class Main {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException, ControllerException {
        if(args.length != 6) {
            System.err.println("To initialize the game you must pass the file with the zones, the file with allied players, the file with axis players, tickets at the start of the game, maximum time for each game in seconds and number of games to be played as arguments.\n" +
                "Example: Main <name_of_zone_file> <name_of_allied_players_file> <name_of_axis_players_file> <number_of_tickets_at_start> <max_time_of_game> <number_of_games>\n" +
                "Restart Main with valid arguments.");
            return;
        }

        String zonesFileName = args[0];
        String alliedFileName = args[1];
        String axisFileName = args[2];
        int initialTickets = Integer.parseInt(args[3]);
        int gameTime = Integer.parseInt(args[4]);
        int games = Integer.parseInt(args[5]);

        SwingGUIStats swingGUIStats = new SwingGUIStats();
        Thread threadStats = new Thread(swingGUIStats);
        threadStats.start();


        Handler fileHandler = new FileHandler("./src/logs/game.log");
        Handler consoleHandler = new ConsoleHandler();

        LOGGER.addHandler(fileHandler);
        LOGGER.addHandler(consoleHandler);

        fileHandler.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.SEVERE);
        LOGGER.setLevel(Level.ALL);

        for (int i = 1; i <= games; i++) {

            SwingGUIGame swingGUIGame = new SwingGUIGame(i, initialTickets, gameTime);
            Thread threadGame = new Thread(swingGUIGame);
            threadGame.start();

            Runtime rt = Runtime.instance();

            Properties props = new ExtendedProperties();
            props.setProperty("gui", "true");
            props.setProperty("main", (Boolean.TRUE).toString());

            Profile p = new ProfileImpl(props);
            ContainerController cc = rt.createMainContainer(p);

            LOGGER.config("Initializing game-" + i);
            Logger.setLogger(LOGGER);

            List<Position> zonePositions = parseZones(zonesFileName);

            if(zonePositions.size() < 2) {
                System.err.println("Number of zones should be at least 3! The first 2 lines are allied spawn position and axis spawn position");
                return;
            }

            List<PlayerClass> alliedPlayers = parsePlayers(alliedFileName);
            List<PlayerClass> axisPlayers = parsePlayers(axisFileName);

            if(alliedPlayers.size() != axisPlayers.size() || alliedPlayers.size() == 0) {
                System.err.println("Number of players should be at least 1!");
                return;
            }

            //start game
            GameServer server = new GameServer(zonePositions.size() - 2, axisPlayers.size(), initialTickets, gameTime, swingGUIGame, swingGUIStats);
            List<AgentController> agentsList = new ArrayList<AgentController>();
            agentsList.add(cc.acceptNewAgent("game-server", server));

            agentsList.add(cc.acceptNewAgent("allied-spawn", new Zone(zonePositions.get(0), ZoneType.BASE, Team.ALLIED, 0, swingGUIGame, swingGUIStats)));
            agentsList.add(cc.acceptNewAgent("axis-spawn", new Zone(zonePositions.get(1), ZoneType.BASE, Team.AXIS, 0, swingGUIGame, swingGUIStats)));

            for (int j = 2; j < zonePositions.size(); j++) {
                char c = (char) ('A' + j - 2);
                agentsList.add(cc.acceptNewAgent("zone-" + c, new Zone(zonePositions.get(j), ZoneType.CAPTURABLE, Team.NEUTRAL, 5, swingGUIGame, swingGUIStats)));
            }

            for (int j = 0; j < alliedPlayers.size(); j++) {
                agentsList.add(cc.acceptNewAgent("allied-" + j + "-" + alliedPlayers.get(j).toString().toLowerCase(), new Player(Team.ALLIED, alliedPlayers.get(j), swingGUIGame, swingGUIStats)));
            }

            for (int j = 0; j < axisPlayers.size(); j++) {
                agentsList.add(cc.acceptNewAgent("axis-" + j + "-" + axisPlayers.get(j).toString().toLowerCase(), new Player(Team.AXIS, axisPlayers.get(j), swingGUIGame, swingGUIStats)));
            }

            System.out.println("To start game type in any letter and press Enter.");
            System.in.read();

            for(AgentController agent : agentsList) {
                agent.start();
            }

            while(!server.isFinished());

            System.in.skip(System.in.available());
            System.out.println("To close this game's UI type in any letter and press Enter.");
            System.in.read();

            cc.kill();
            rt.shutDown();
            swingGUIGame.closeSwingGUI();
        }

        swingGUIStats.closeSwingGUI();
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
}
