package gui.components;

import agents.Player;
import data.Position;
import data.Team;
import jade.core.AID;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ZoneInformationPanel extends JPanel {
    private final Font font = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private final Font font16 = new Font(Font.MONOSPACED, Font.BOLD, 20);
    private final static int TITLE_PANEL_WIDTH = 200;
    private final static int TITLE_PANEL_HEIGHT = 20;
    private final static int WIDTH = 600;
    private final static int HEIGHT = 885;

    private JList zoneInfoList;
    private JScrollPane zoneInfoPane;
    private List<String> zoneInfo;

    private Map<AID, Position> zonePosition = new HashMap<>();
    private Map<AID, Team> zoneTeam = new HashMap<>();
    private Map<AID, Team> playerTeam = new HashMap<>();
    private Map<AID, AID> playerZone = new HashMap<>();
    private Map<AID, Integer> playerHealth = new HashMap<>();
    private Map<AID, AID> playerMovingTo = new HashMap<>();


    public ZoneInformationPanel(){
        this.setBounds(0, 115, WIDTH, HEIGHT);
        this.setLayout(null);
        this.setTitle();
        this.setZoneInfo();
    }

    private void setTitle() {
        JLabel title = new JLabel("Zone Information", SwingConstants.CENTER);
        title.setBounds(this.getWidth() / 2 - (TITLE_PANEL_WIDTH / 2), 0, TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT);
        title.setFont(font16);
        this.add(title);
    }

    private void setZoneInfo(){
        zoneInfoList = new JList();
        zoneInfoPane = new JScrollPane(zoneInfoList);
        zoneInfoPane.setBounds(new Rectangle(30, 30, WIDTH - 60, this.getHeight() - 80));
        this.add(zoneInfoPane);
    }

    public synchronized void addUpdateNewZone(AID zone, Team team, Position position){
        if(!zoneTeam.containsKey(zone)) {
            zonePosition.put(zone, position);
            zoneTeam.put(zone, team);
        }
        else{
            zoneTeam.replace(zone, team);
        }
        this.updatePane();
    }

    public synchronized void addUpdatePlayer(Player player){
        playerTeam.put(player.getAID(), player.getTeam());
        playerZone.put(player.getAID(), player.getCurrentZone());
        playerHealth.put(player.getAID(), player.getHealth());
        this.updatePane();
    }

    public synchronized  void addPlayerMovingToZone(AID player, AID zone, int time){
        this.playerMovingTo.put(player, zone);
    }

    public synchronized void removePlayerMovingToZone(AID player){
        this.playerMovingTo.remove(player);
    }

    private synchronized void updatePane(){
        zoneInfo = new ArrayList<>();
        List<String> zoneInfoOrdered = new ArrayList<>();

        zoneTeam.forEach((aid, team) -> {

            final String[] string = new String[1];

            string[0] = "Zone Name: " + aid.getLocalName() + "  Position : " + zonePosition.get(aid) + "  N axis: " + numberOfPlayersZone(aid, Team.AXIS) +
                    "  N allied: " + numberOfPlayersZone(aid, Team.ALLIED) + "\n";

            if(!aid.getLocalName().equals("allied-spawn")) {
                string[0] += "    Axis:\n";
                playerZone.forEach((player, playerZone) -> {
                    if (aid.equals(playerZone) && playerTeam.get(player).equals(Team.AXIS)) {
                        string[0] += "    " + player.getLocalName() + "  hp:" + playerHealth.get(player) + "\n";
                    }
                });
                for(int i = 0; i < 2; i++)
                    string[0] += " \n";
            }

            if(!aid.getLocalName().equals("axis-spawn")) {
                string[0] += "   Allied:\n";
                playerZone.forEach((player, playerZone) -> {
                    if (aid.equals(playerZone) && playerTeam.get(player).equals(Team.ALLIED)) {
                        string[0] += "    " + player.getLocalName() + "  hp:" + playerHealth.get(player) + "\n";
                    }
                });
                for(int i = 0; i < 2; i++)
                    string[0] += " \n";
            }
            string[0] += aid.getLocalName().matches(".*spawn.*") ?  "   Respawning:\n" : "   Moving To:\n";

            playerMovingTo.forEach((player, playerZone) -> {
                if(aid.equals(playerZone)){
                    string[0] += "    " + player.getLocalName() + "  hp:" + playerHealth.get(player) + "\n";
                }
            });

            for(int i = 0; i < 4; i++)
                string[0] += " \n";

            zoneInfo.add(string[0]);
        });

        zoneInfo.sort(String::compareTo);


        for(String zone : zoneInfo){
            zoneInfoOrdered.addAll(Arrays.asList(zone.split("\n")));
        }

        zoneInfoList.setListData(zoneInfoOrdered.toArray());
        zoneInfoPane.revalidate();
        this.revalidate();
    }
    
    private synchronized int numberOfPlayersZone(AID zone, Team team){
        AtomicInteger number = new AtomicInteger();

        playerZone.forEach((player, playerZone) -> {
            if(zone.equals(playerZone) && playerTeam.get(player) == team){
               number.incrementAndGet();
            }
        });

        return number.get();
    }


}
