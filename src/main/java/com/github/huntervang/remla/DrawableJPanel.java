package com.github.huntervang.remla;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawableJPanel extends JPanel {

    private List<BuildStage> buildStages = new ArrayList<>();

    public void setBuildStaes(List<BuildStage> stages) {
        buildStages = stages;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Component depthLayer : this.getComponents()) {
            for (Component stageBtn : ((JPanel) depthLayer).getComponents()) {
                Point source = SwingUtilities.convertPoint(depthLayer, stageBtn.getX() + stageBtn.getWidth(), stageBtn.getY() + stageBtn.getHeight() / 2, this);
                List<Point> destination = getLineDestinations(((JButton) stageBtn).getText());
                destination.forEach(dest -> g.drawLine(source.x, source.y, dest.x, dest.y));

            }
        }

    }

    private List<Point> getLineDestinations(String stageName) {
        for (BuildStage stage : buildStages) {
            if (stage.getName().equals(stageName)) {
                List<Point> destinations = new ArrayList<>();
                stage.getChildren().forEach(child -> destinations.add(getButtonLocation(child.getName())));
                return destinations;
            }
        }
        return null;
    }

    private Point getButtonLocation(String stageName) {
        for (Component depthLayer : this.getComponents()) {
            for (Component stageBtn : ((JPanel) depthLayer).getComponents()) {
                if (((JButton) stageBtn).getText().equals(stageName)) {
                    return SwingUtilities.convertPoint(depthLayer, stageBtn.getX(), stageBtn.getY() + stageBtn.getHeight() / 2, this);
                }
            }
        }
        return null;
    }
}
