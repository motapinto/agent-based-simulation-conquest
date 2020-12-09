import Execute.Launcher;
import gui.SwingGUIStats;
import uchicago.src.sim.engine.SimInit;
public class Main {

    public static void main(String[] args) {

        SwingGUIStats swingGUIStats = new SwingGUIStats();
        Thread threadStats = new Thread(swingGUIStats);
        threadStats.start();

        Launcher l = new Launcher(swingGUIStats);
        Thread thread = new Thread(l);
        thread.start();

        while(!l.isStopSimulation());

        swingGUIStats.closeSwingGUI();
    }
}
