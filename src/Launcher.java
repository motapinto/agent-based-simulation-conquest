import gui.SwingGUIGame;
import gui.SwingGUIStats;
import sajas.sim.repast3.Repast3Launcher;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import agents.GameServer;
import agents.Player;
import agents.Zone;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;

public class Launcher extends Repast3Launcher {
    private ContainerController controller;

    @Override
    public String[] getInitParam() {
        return new String[] {"A", "B", "C", "D", "E"};
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
    }

    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        this.container = rt.createAgentContainer(p1);

        try {
            launchAgents(container);
        } catch (StaleProxyException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void assembleGameServer() throws IOException {
        String zonesFileName = "1.txt";
        String alliedFileName = "1.txt";
        String axisFileName = "2.txt";
        int initialTickets = 200;
        int gameTime = 50;
        int games = 1;

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

        SwingGUIGame swingGUIGame = new SwingGUIGame(0, initialTickets, gameTime);
        Thread threadGame = new Thread(swingGUIGame);
        threadGame.start();

        GameServer server = new GameServer(zonePositions.size() - 2, axisPlayers.size(), initialTickets, gameTime, swingGUIGame, swingGUIStats);
    }

    private void launchAgents(ContainerController container) throws StaleProxyException, FileNotFoundException {



        GameServer server = GameServer.newInstance();

        //Set the parameters of execution
        Parameters params = RunEnvironment.getInstance().getParameters();
        server.setCTHealth((int) params.getValue("CT_Health"));
        server.setTHealth((int) params.getValue("T_Health"));
        server.setMinDmg((int) params.getValue("Min_Dmg"));
        server.setMaxDmg((int) params.getValue("Max_Dmg"));
        server.setCritDmg((int) params.getValue("Crit_Dmg"));
        server.setCritChance((int) params.getValue("Crit_Chance"));
        server.setFirstStrat((String) params.getValue("First_Strat"));

        RunEnvironment.getInstance().endAt(50000000);

        container.acceptNewAgent("server", server).start();
        context.add(server);

        int iglIndex = ThreadLocalRandom.current().nextInt(0, 5);

        for (int i = 0; i < 5; i++) {
            boolean isIGL = false, hasBomb = false;

            if (iglIndex == i) isIGL = true;
            if (i == 0) hasBomb = true;

            CTAgent ct = new CTAgent(this.space, this.grid, isIGL);
            TAgent t = new TAgent(this.space, this.grid, isIGL, hasBomb);

            container.acceptNewAgent("CT" + (i+1), ct).start();
            container.acceptNewAgent("T" + (i+1), t).start();
            context.add(ct); context.add(t);

            GridPoint ctSpawnPoint = server.generateSpawnPoint(true), tSpawnPoint = server.generateSpawnPoint(false);

            space.moveTo(ct, ctSpawnPoint.getX(), ctSpawnPoint.getY());
            space.moveTo(t, tSpawnPoint.getX(), tSpawnPoint.getY());
        }

        BombAgent bomb = new BombAgent(this.grid, this.space);
        container.acceptNewAgent("bomb", bomb).start();
        context.add(bomb);

        for (Object obj : context) {
            NdPoint pt = space.getLocation(obj);
            grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
        }
    }
}