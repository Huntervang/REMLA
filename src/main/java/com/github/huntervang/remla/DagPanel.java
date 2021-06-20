package com.github.huntervang.remla;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DagPanel extends JPanel {

    private List<BuildStage> buildStages = new ArrayList<>();

    public void setBuildStages(List<BuildStage> stages) {
        buildStages = stages;
    }

    private String previousStage = null;

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

    public void startPipeline() {
        previousStage = null;
        for (Component depthLayer : this.getComponents()) {
            for (Component stageBtn : ((JPanel) depthLayer).getComponents()) {
                ((JButton) stageBtn).setBackground(JBColor.GRAY);
                ((JButton) stageBtn).setOpaque(true);
            }
        }
    }

    public void runStage(String stageName) {
        setColour(stageName, JBColor.BLUE);
    }

    public void skipStage(String stageName) {
        setColour(stageName, JBColor.GREEN);
    }

    public void cacheStage(String stageName) {
        setColour(stageName, JBColor.GREEN);
    }

    public void failStage(String stageName) {

    }

    public void passStage(String stageName) {

    }

    private void setColour(String stageName, JBColor color) {
        for (Component depthLayer : this.getComponents()) {
            for (Component stageBtn : ((JPanel) depthLayer).getComponents()) {
                if (((JButton) stageBtn).getText().equals(stageName)) {
                    ((JButton) stageBtn).setBackground(color);
                    ((JButton) stageBtn).setOpaque(true);
                }
                if (((JButton) stageBtn).getText().equals(previousStage)) {
                    ((JButton) stageBtn).setBackground(JBColor.GREEN);
                    ((JButton) stageBtn).setOpaque(true);
                    previousStage = stageName;
                }
            }
        }
        if (previousStage == null) {
            previousStage = stageName;
        }
    }

    public void finishLastStage() {
        setColour(previousStage, JBColor.GREEN);
    }
}
