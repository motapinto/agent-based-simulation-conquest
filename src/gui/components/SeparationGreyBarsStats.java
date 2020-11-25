package gui.components;

import javax.swing.*;
import java.awt.*;

public class SeparationGreyBarsStats extends JPanel {
    public SeparationGreyBarsStats(){
        this.setLayout(null);
        this.setBounds(0, 0, 700, 500);
        JPanel panel1 = new JPanel();
        panel1.setBounds(new Rectangle(350 + 200, 0, 5 , 500));
        panel1.setBackground(Color.GRAY);
        this.add(panel1);
    }
}