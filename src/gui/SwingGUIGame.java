package gui;

import gui.components.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SwingGUIGame implements  Runnable {
    private final JFrame frame;
    private final JButton teamCompButton;

    private final JList logsList;
    private final JScrollPane pane;
    private final GameTimePointsPanel gameTimeAndPointsPanel;
    private final ZoneMapPanel zoneMapPanel;
    private int gameNumber;
    private TeamCompPanel teamCompPanel;

    public final static Color GREEN = new Color(46,139,87);
    public final static Color RED = new Color(240,128,128);

    private ZoneInformationPanel zoneInformationPanel;
    private final List<String> logs;
    private final SeparationGreyBarsGame separationGreyBarsGame;
    private final Font font15 = new Font("SansSerif", Font.PLAIN, 13);
    private final Font font17 = new Font(Font.MONOSPACED, Font.PLAIN, 17);

    public SwingGUIGame(int gameNumber, int initialPoints, int gameTime){
        frame = new JFrame("Current Game");
        teamCompPanel = new TeamCompPanel();
        zoneInformationPanel = new ZoneInformationPanel();
        separationGreyBarsGame = new SeparationGreyBarsGame();

        this.gameNumber = gameNumber;

        gameTimeAndPointsPanel = new GameTimePointsPanel(gameNumber, initialPoints, initialPoints, gameTime);
        zoneMapPanel = new ZoneMapPanel();

        logs = new ArrayList<>();
        logsList = new JList(logs.toArray());
        pane = new JScrollPane(logsList);
        pane.setBounds(new Rectangle(625, 300, 350, 650));
        pane.setFont(font15);
        frame.add(pane);

        teamCompButton = new JButton("Zone information");
        teamCompButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.remove(separationGreyBarsGame);
                if(teamCompButton.getText().equals("Team Comp")) {
                    teamCompButton.setText("Zone information");
                    frame.remove(zoneInformationPanel);
                    frame.add(teamCompPanel);
                }
                else {
                    teamCompButton.setText("Team Comp");
                    frame.remove(teamCompPanel);
                    frame.add(zoneInformationPanel);
                }
                frame.add(separationGreyBarsGame);
                frame.repaint();
                frame.revalidate();
            }
        });

        teamCompButton.setBounds(700, 150, 200, 60);
        teamCompButton.setFont(font17);

        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void run() {
        JLabel logs = new JLabel("Player Logs", SwingConstants.CENTER);
        logs.setBounds(700, 270, 200, 30);
        logs.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        frame.add(logs);
        frame.add(gameTimeAndPointsPanel);
        frame.add(teamCompButton);
        frame.add(zoneMapPanel);
        frame.add(teamCompPanel);
        frame.add(separationGreyBarsGame);
        frame.setSize(1500, 1000);
        frame.setLayout(null);//using no layout managers
        frame.setVisible(true);//making the frame visible
    }

    public void closeSwingGUI(){
        frame.dispose();
    }

    public synchronized void addNewLog(String newLog){
        JScrollBar sb = pane.getVerticalScrollBar();
        int currentValue = sb.getValue();
        logs.add(logs.size() + " : " + newLog);
        logsList.setListData(logs.toArray());
        logsList.revalidate();
        pane.revalidate();
        sb = pane.getVerticalScrollBar();
        if(currentValue + 700 >= sb.getMaximum()){
            sb.setValue(sb.getMaximum());
        }
        frame.revalidate();
        frame.repaint();
    }

    public ZoneMapPanel getZoneMapPanel() { return zoneMapPanel; }

    public void startGame(){ gameTimeAndPointsPanel.startTimer(); }

    public GameTimePointsPanel getGameTimeAndPointsPanel() { return gameTimeAndPointsPanel; }

    public int getGameNumber() { return gameNumber; }

    public TeamCompPanel getTeamCompPanel() { return teamCompPanel; }

    public ZoneInformationPanel getZoneInformationPanel() { return zoneInformationPanel; }
}
