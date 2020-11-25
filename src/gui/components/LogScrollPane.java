package gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LogScrollPane extends JScrollPane {

    private JList logsList;
    private List<String> logs;

    private final Font font15 = new Font("SansSerif", Font.BOLD, 13);

    public LogScrollPane(){
        this.setLayout(null);
        this.setBounds(new Rectangle(625, 300, 350, 650));
        this.setFont(font15);
        logs = new ArrayList<>();
        logsList = new JList(logs.toArray());
        this.setViewportView(logsList);

    }

    public void addNewLog(String newLog){
        JScrollBar sb = this.getVerticalScrollBar();
        int currentValue = sb.getValue();
        logs.add(logs.size() + " : " + newLog);
        logsList.setListData(logs.toArray());
        logsList.revalidate();
        this.revalidate();
        sb = this.getVerticalScrollBar();
        if(currentValue + 700 >= sb.getMaximum()){
            sb.setValue(sb.getMaximum());
        }
        this.revalidate();
    }
}
