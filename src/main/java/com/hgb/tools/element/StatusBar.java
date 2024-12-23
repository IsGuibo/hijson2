package com.hgb.tools.element;

import org.fife.rsta.ui.SizeGripIcon;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel label;

    public StatusBar() {
        label = new JLabel("");
        setLayout(new BorderLayout());
        add(label, BorderLayout.LINE_START);
        add(new JLabel(new SizeGripIcon()), BorderLayout.LINE_END);
    }

    public void setLabel(String label) {
        setLabel(label, Color.BLACK);
    }

    public void setLabel(String label, Color color) {
        this.label.setForeground(color);
        this.label.setText(label);
    }

}