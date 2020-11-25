package gui.components;

import gui.SwingGUIGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameTimePointsPanel extends JPanel {
    private JLabel axisPointsLabel;
    private int gameTime;
    private JLabel alliedPointsLabel;
    private JLabel gameTimeLabel;

    private final static int X = 0;
    private final static int Y = 0;
    private final static int WIDTH = 1500;
    private final static int HEIGHT = 110;
    private final static int X_FACTOR = 50;

    private final static Font FONT = new Font(Font.MONOSPACED, Font.BOLD, 20);

    private void writeTitleLabel(int gameNumber) {
        final int panelWidth = 200;
        final int panelHeight = 20;
        final int fontSize = 25;

        JLabel title = new JLabel("Game number " + gameNumber, SwingConstants.CENTER);
        title.setBounds(WIDTH / 2 - (panelWidth / 2) + X_FACTOR, 0, panelWidth, panelHeight);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
        this.add(title);
    }

    private void writeAxisPoints(int points) {
        final int panelWidth = 200;
        final int panelHeight = 20;
        final int fontSize = 20;

        this.axisPointsLabel = new JLabel("Axis => " + points, SwingConstants.CENTER);
        this.axisPointsLabel.setBounds(WIDTH / 2 - (panelWidth / 2) - X_FACTOR, 80, panelWidth, panelHeight);
        this.axisPointsLabel.setForeground(SwingGUIGame.RED);
        this.axisPointsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
        this.add(this.axisPointsLabel);
    }

    private void writeVsLabel() {
        final int panelWidth = 35;
        final int panelHeight = 20;
        final int fontSize = 20;

        JLabel vs = new JLabel("vs", SwingConstants.CENTER);
        vs.setBounds(WIDTH / 2 + (X_FACTOR / 2), 80, panelWidth, panelHeight);
        vs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        this.add(vs);
    }

    private void writeAlliedPoints(int points) {
        final int panelWidth = 200;
        final int panelHeight = 20;
        final int fontSize = 20;

        this.alliedPointsLabel = new JLabel(points + " <= Allied", SwingConstants.CENTER);
        this.alliedPointsLabel.setBounds(WIDTH / 2 + (panelWidth / 2) - X_FACTOR, 80, panelWidth, panelHeight);
        this.alliedPointsLabel.setForeground(SwingGUIGame.GREEN);
        this.alliedPointsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
        this.add(this.alliedPointsLabel);
    }

    private void writeGameTime(int totalGameTimes) {
        final int panelWidth = 300;
        final int panelHeight = 20;
        final int fontSize = 20;

        this.gameTime = totalGameTimes;
        this.gameTimeLabel = new JLabel("Game time (s): " + (this.gameTime), SwingConstants.CENTER);
        this.gameTimeLabel.setBounds(WIDTH / 2 - (panelWidth / 2) + X_FACTOR, 40, panelWidth, panelHeight);
        this.gameTimeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
        this.add(this.gameTimeLabel);
    }

    public GameTimePointsPanel(int gameNumber, int axisPoints, int alliedPoints, int gameTime){
        this.setBounds(X, Y, WIDTH, HEIGHT);
        this.setLayout(null);

        this.writeTitleLabel(gameNumber);
        this.writeAxisPoints(axisPoints);
        this.writeVsLabel();
        this.writeAlliedPoints(alliedPoints);
        this.writeGameTime(gameTime);
    }



    public void startTimer(){
        Timer t = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(gameTime > 0) {
                    gameTime = gameTime - 1;
                    gameTimeLabel.setText("Game time (s): " + (gameTime));
                    gameTimeLabel.revalidate();
                }
            }
        });
        t.start();
    }

    public void setAlliedPoints(int alliedPoints) {
        this.alliedPointsLabel.setText(alliedPoints + " <= Allied");
        this.alliedPointsLabel.revalidate();
        this.revalidate();
    }

    public void setAxisPoints(int axisPoints) {
        this.axisPointsLabel.setText("Axis => " + axisPoints);
        this.axisPointsLabel.revalidate();
        this.revalidate();
    }
}
