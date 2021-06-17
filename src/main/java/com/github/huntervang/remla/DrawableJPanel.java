package com.github.huntervang.remla;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawableJPanel extends JPanel {
    private List<Integer> a = new ArrayList<>();
    private List<Integer> b = new ArrayList<>();
    private List<Integer> c = new ArrayList<>();
    private List<Integer> d = new ArrayList<>();
    private List<Integer> widths = new ArrayList<>();
    private List<Color> colors = new ArrayList<>();
    private List<JButton> buttons = new ArrayList<>();

    public void addLine(int x1, int y1, int x2, int y2, int width, Color color) {
        a.add(x1);
        b.add(y1);
        c.add(x2);
        d.add(y2);
        widths.add(width);
        colors.add(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Component depthLayer : this.getComponents()) {
            for (Component stageBtn : ((JPanel) depthLayer).getComponents()) {
                Point loc = SwingUtilities.convertPoint(depthLayer, stageBtn.getX(), stageBtn.getY(), this);
                g.drawLine(loc.x, loc.y, 100, 100);
            }
        }

    }
}
