package gui.components;

import data.Position;
import data.Team;
import gui.SwingGUIGame;
import jade.core.AID;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ZoneMapPanel extends JPanel {
    private final static int X = 1005;
    private final static int Y = 115;
    private final static int WIDTH = 500;
    private final static int HEIGHT = 680;

    private final static int TITLE_PANEL_HEIGHT = 40;
    private final static int ZONE_RADIUS = 80;



    private final Map<AID, Position> zonePosition = new HashMap<>();
    private final Map<AID, Double> zonePoints = new HashMap<>();
    private final Map<AID, Team> zoneTeam = new HashMap<>();
    private final Map<String, JLabel> zonePointsLabel = new HashMap<>();

    public ZoneMapPanel() {
        this.setBounds(X, Y, WIDTH, HEIGHT);
        this.setLayout(null);
        this.writeTitleLabel();
    }

    private void writeTitleLabel() {
        final int panelWidth = 250;
        final int fontSize = 40;

        JLabel title = new JLabel("Zone's MAP", SwingConstants.CENTER);
        title.setBounds(WIDTH / 2 - (panelWidth / 2), 0, panelWidth, TITLE_PANEL_HEIGHT);
        title.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        this.add(title);
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.zoneTeam.forEach((aid, team) -> {
            this.paintZone(g, this.zonePosition.get(aid), team, aid.getLocalName(), this.zonePoints.get(aid));
        });
    }

    private void paintZone(Graphics g, Position position, Team team, String s, double points) {
        Color color;
        switch (team) {
            case ALLIED: color = SwingGUIGame.GREEN; break;
            case AXIS: color = SwingGUIGame.RED; break;
            case NEUTRAL: color = Color.GRAY; break;
            default: color = Color.BLACK; break;
        }

        g.drawOval(position.getX() - ZONE_RADIUS/2, position.getY() + TITLE_PANEL_HEIGHT, ZONE_RADIUS, ZONE_RADIUS);
        g.setColor(color);
        g.fillOval(position.getX() - ZONE_RADIUS/2, position.getY() + TITLE_PANEL_HEIGHT, ZONE_RADIUS, ZONE_RADIUS);

        this.zonePointsLabel.get(s).setText(String.valueOf(points));
    }

    private void writeZoneNameLabel(String s, Position pos) {
        final int panelWidth = 150;
        final int panelHeight = 20;
        final int fontSize = 20;

        JLabel label = new JLabel(s, SwingConstants.CENTER);
        label.setBounds(pos.getX() - panelWidth/2, pos.getY() + panelHeight/2, panelWidth, panelHeight);
        Font zoneNameFont = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        label.setFont(zoneNameFont);
        this.add(label);
    }

    private void writeZonePointsLabel(String name, Position pos, double points) {
        final int panelWidth = 75;
        final int panelHeight = ZONE_RADIUS;
        final int fontSize = 15;

        JLabel label = new JLabel(String.valueOf(points), SwingConstants.CENTER);
        label.setBounds(pos.getX() - panelWidth/2, pos.getY() + panelHeight/2, panelWidth, panelHeight);
        Font zoneNameFont = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
        label.setFont(zoneNameFont);
        this.add(label);
        this.zonePointsLabel.put(name, label);
    }

    public synchronized void addUpdateZone(AID zone, Team team, Position position, double points) {
        if(!this.zoneTeam.containsKey(zone)) {
            this.zoneTeam.put(zone, team);
            this.zonePoints.put(zone, points);
            this.zonePosition.put(zone, position);

            this.writeZoneNameLabel(zone.getLocalName(), position);
            this.writeZonePointsLabel(zone.getLocalName(), position, points);
        } else {
            this.zoneTeam.replace(zone, team);
            this.zonePoints.replace(zone, points);
            this.zonePosition.replace(zone, position);
        }

        this.repaint();
        this.revalidate();
    }
}
