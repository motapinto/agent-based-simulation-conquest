package agents;

import gui.SwingGUIGame;
import gui.SwingGUIStats;

import java.util.logging.Level;

public abstract class Logger extends DirectoryFacilitator {
    private static java.util.logging.Logger logger;

    protected final SwingGUIGame swingGUIGame;
    protected final SwingGUIStats swingGUIStats;

    protected Logger(SwingGUIGame swingGUIGame, SwingGUIStats swingGUIStats) {
        this.swingGUIGame = swingGUIGame;
        this.swingGUIStats = swingGUIStats;
    }

    public static void setLogger(java.util.logging.Logger logger) {
        Logger.logger = logger;
    }

    public void logEnd(String message) {
        Logger.logger.log(Level.OFF, message);
        this.swingGUIGame.addNewLog(message);
    }

    public void logConfig(String message) {
        Logger.logger.config(message);
        this.swingGUIGame.addNewLog(message);
    }

    public void logAction(String message) {
        Logger.logger.fine(message);
        this.swingGUIGame.addNewLog(message);
    }

    public void logAction(String message, Object object) {
        Logger.logger.log(Level.FINE, message, object);
        this.swingGUIGame.addNewLog(message + " With: " + object.toString());
    }

    public void logException(String message, Throwable exception) {
        Logger.logger.log(Level.WARNING, message, exception);
        this.swingGUIGame.addNewLog(message + " With exception: " + exception.getMessage());
    }

    public SwingGUIGame getSwingGUIGame() { return swingGUIGame; }
    public SwingGUIStats getSwingGUIStats() { return swingGUIStats; }
}
