package gui;

import data.Team;
import gui.components.SeparationGreyBarsStats;
import gui.data.GameStats;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SwingGUIStats implements  Runnable {
    private final List<GameStats> gamesStats = new ArrayList<>();
    private final JFrame frame;
    private final static Font font = new Font("Monospaced", Font.PLAIN, 15);

    private JLabel axisWinRate;
    private JLabel alliedWinRate;

    private JList logsList;
    private JScrollPane pane;

    private List<GameStats> gameStats = new ArrayList<>();

    public SwingGUIStats(){
        frame = new JFrame("Stats about games played");
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void run() {
        JLabel lastGames, stats, winRate;

        lastGames = new JLabel("Last Games", SwingConstants.CENTER);
        lastGames.setBounds(220, 20, 100, 30);
        lastGames.setFont(font);

        stats = new JLabel("Stats",  SwingConstants.CENTER);
        stats.setBounds(425 + 200, 20, 100, 20);
        stats.setFont(font);


        winRate = new JLabel("Win Rate",  SwingConstants.CENTER);
        winRate.setBounds(425 +200, 60, 100, 20);
        winRate.setForeground(Color.BLACK);
        winRate.setFont(font);

        axisWinRate = new JLabel("Axis=>0%");
        axisWinRate.setBounds(370 + 200, 90, 120, 20);
        axisWinRate.setForeground(Color.RED);
        axisWinRate.setFont(font);

        alliedWinRate = new JLabel("0%<=Allied");
        alliedWinRate.setBounds(490 + 200, 90, 120, 20);
        alliedWinRate.setForeground(Color.BLUE);
        alliedWinRate.setFont(font);

        JLabel vs = new JLabel("vs", SwingConstants.CENTER);
        vs.setBounds(455 + 200, 90, 40, 20);
        vs.setFont(font);

        SeparationGreyBarsStats separationGreyBarsStats = new SeparationGreyBarsStats();

        logsList = new JList(gamesStats.toArray());
        pane = new JScrollPane(logsList);
        pane.setBounds(new Rectangle(25, 60, 500, 400));
        pane.setFont(font);

        frame.add(pane);
        frame.add(vs);
        frame.add(axisWinRate);
        frame.add(alliedWinRate);
        frame.add(winRate);
        frame.add(lastGames);
        frame.add(stats);
        frame.add(separationGreyBarsStats);

        frame.setLayout(null);
        frame.setSize(810,520);
        // Set the window to be visible as the default to be false
        frame.setVisible(true);//making the frame visible

    }

    public void closeSwingGUI(){
        frame.dispose();
    }

    public void addNewGame(int alliedPoints, int axisPoints, int gameNumber, Team team){

        gamesStats.add(new GameStats(alliedPoints, axisPoints, gameNumber, team));

        JScrollBar sb = pane.getVerticalScrollBar();
        int currentValue = sb.getValue();

        logsList.setListData(gamesStats.toArray());
        logsList.revalidate();
        pane.revalidate();
        sb = pane.getVerticalScrollBar();
        if(currentValue + 600 >= sb.getMaximum()){
            sb.setValue(sb.getMaximum());
        }

        axisWinRate.setText("Axis=>" + String.format("%.0f", calculateWinRate(Team.AXIS))  + "%");
        axisWinRate.revalidate();
        alliedWinRate.setText(String.format("%.0f", calculateWinRate(Team.ALLIED))  + "%<=Allied");
        axisWinRate.revalidate();
        frame.revalidate();
        frame.repaint();
    }

    private double calculateWinRate(Team team){
        int numberOfGamesWon = 0;
        for(GameStats gameStats: gamesStats){
            if(gameStats.winner == team){
                numberOfGamesWon++;
            }
        }
        return (double) numberOfGamesWon/gamesStats.size() * 100;
    }
}
