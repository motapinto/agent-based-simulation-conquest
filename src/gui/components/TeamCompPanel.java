package gui.components;

import data.PlayerClass;
import data.Team;
import gui.SwingGUIGame;
import jade.core.AID;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TeamCompPanel extends JPanel {
    private final Font font = new Font(Font.MONOSPACED, Font.BOLD, 20);
    private final static int TITLE_PANEL_WIDTH = 200;
    private final static int TITLE_PANEL_HEIGHT = 20;
    private final static int WIDTH = 600;
    private final static int HEIGHT = 885;

    private final Map<AID, Team> agentTeam = new HashMap<>();
    private final Map<AID, Integer> agentPoints = new HashMap<>();
    private final Map<AID, PlayerClass> agentClass = new HashMap<>();

    private JList axisTeamCompList;
    private JScrollPane axisTeamCompPane;
    private JList alliedTeamCompList;
    private JScrollPane alliedTeamCompPane;
    List<String> axisTeamComp;
    List<String> alliedTeamComp;

    public TeamCompPanel(){
        this.setBounds(0, 115, WIDTH, HEIGHT);
        this.setLayout(null);
        this.setTitle();
        this.setTeams();
    }

    private void setTitle() {
        JLabel title = new JLabel("Team Composition", SwingConstants.CENTER);
        title.setBounds(this.getWidth() / 2 - (TITLE_PANEL_WIDTH / 2), 0, TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT);
        title.setFont(font);
        this.add(title);
    }

    private void setTeams(){
        JLabel axis = new JLabel("Axis", SwingConstants.CENTER);
        axis.setBounds(this.getWidth()/2 - TITLE_PANEL_WIDTH/2 , 30, TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT);
        axis.setFont(font);
        axis.setForeground(SwingGUIGame.RED);
        this.add(axis);

        JLabel allied = new JLabel("Allied", SwingConstants.CENTER);
        allied.setBounds(this.getWidth()/2 - TITLE_PANEL_WIDTH/2, this.getHeight() / 2, TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT);
        allied.setFont(font);
        allied.setForeground(SwingGUIGame.GREEN);
        this.add(allied);

        axisTeamCompList = new JList();
        axisTeamCompPane = new JScrollPane(axisTeamCompList);
        alliedTeamCompList = new JList();
        alliedTeamCompPane = new JScrollPane(alliedTeamCompList);

        axisTeamCompPane.setBounds(new Rectangle(30, 50, WIDTH - 60, this.getHeight()/2 - 60));
        alliedTeamCompPane.setBounds(new Rectangle(30, this.getHeight()/2 + 20 , WIDTH - 60, this.getHeight()/2 - 60));

        this.add(axisTeamCompPane);
        this.add(alliedTeamCompPane);
    }

    public synchronized void addUpdateTeamPlayer(Team team, AID player, int points, PlayerClass playerClass) {
        if(!agentPoints.containsKey(player)){
            agentTeam.put(player, team);
            agentPoints.put(player, points);
            agentClass.put(player, playerClass);
        }
        else{
            agentPoints.replace(player, points);
        }

        axisTeamComp = new ArrayList<>();
        alliedTeamComp = new ArrayList<>();

        agentTeam.forEach((playerAgent, playerTeam) -> {
            if(playerTeam == Team.ALLIED){
                alliedTeamComp.add("Agent Name: " + playerAgent.getLocalName() + " Players Class: " + agentClass.get(playerAgent) + " Points: " + agentPoints.get(playerAgent));
            }
            else if(playerTeam == Team.AXIS){
                axisTeamComp.add("Agent Name: " + playerAgent.getLocalName() + " Players Class: " + agentClass.get(playerAgent) + " Points: " + agentPoints.get(playerAgent));
            }
        });

        alliedTeamComp.sort(String::compareTo);
        axisTeamComp.sort(String::compareTo);


        axisTeamCompList.setListData(axisTeamComp.toArray());
        alliedTeamCompList.setListData(alliedTeamComp.toArray());
        alliedTeamCompList.revalidate();
        axisTeamCompList.revalidate();

        alliedTeamCompPane.revalidate();
        axisTeamCompPane.revalidate();
    }


}
