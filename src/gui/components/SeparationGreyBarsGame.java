package gui.components;

import javax.swing.*;
import java.awt.*;

public class SeparationGreyBarsGame extends JPanel {

    public SeparationGreyBarsGame(){

        this.setLayout(null);
        this.setBounds(0, 0, 1500, 1000);

        JPanel panel1 = new JPanel();
        panel1.setBounds(new Rectangle(0, 110, 1500, 5));
        panel1.setBackground(Color.GRAY);

        JPanel panel2 = new JPanel();
        panel2.setBounds(new Rectangle(600, 110, 5, 890));
        panel2.setBackground(Color.GRAY);

        JPanel panel3 = new JPanel();
        panel3.setBounds(new Rectangle(600, 250, 400, 5));
        panel3.setBackground(Color.GRAY);

        JPanel panel4 = new JPanel();
        panel4.setBounds(new Rectangle(1000, 110, 5, 1000));
        panel4.setBackground(Color.GRAY);

        this.add(panel1);
        this.add(panel2);
        this.add(panel3);
        this.add(panel4);

    }
}
