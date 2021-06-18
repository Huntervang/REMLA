package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DVCDag {
    private JPanel rootPanel;
    private DagPanel dagPanel;
    private JButton reproButton;
    private JList<String> stageList;
    private final Project project;
    private final Pipeline pipeline;

    private String activeStage = "";

    private HashMap<String, Object> pipeLine;

    public DVCDag(@NotNull Project newProject) {
        project = newProject;
        pipeline = parseYaml();
        if (this.pipeline != null) {
            drawDag();
        }
        reproButton.addActionListener(e -> repro());
        reproButton.setText("Reproduce Pipeline");
        getPipeline(project.getBasePath() + "/dvc.yaml");
    }

    private void updateStage(String stageName) {
        activeStage = stageName;
        System.out.println("setting: " + stageName);
    }

    private void drawDag() {
        dagPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 80, 20));

        List<BuildStage> rootNodes = new ArrayList<>();
        dagPanel.setBuildStages(pipeline.getStages());
        for (BuildStage stage : pipeline.getStages()) {
            if (stage.getDeps().size() <= 1) {
                rootNodes.add(stage);
            }
        }
        for (BuildStage stage : pipeline.getStages()) {
            for (BuildStage stage_ : pipeline.getStages()) {
                if (!stage.equals(stage_) && intersects(stage.getOuts(), stage_.getDeps())) {
                    stage.addChild(stage_);
                }
            }
        }

        for (BuildStage stage : rootNodes) {
            JButton rootBtn = addJButton(stage.getName());
            JPanel rootPanel = new JPanel();
            rootPanel.add(rootBtn);
            dagPanel.add(rootPanel);
            List<BuildStage> queue = new ArrayList<>();
            queue.add(stage);
            queue.add(null);
            JPanel depthPanel = new JPanel();
            depthPanel.setLayout(new GridLayout(0, 1, 0, 80));
            List<String> visited = new ArrayList<>();
            while (!queue.isEmpty()) {
                BuildStage current = queue.remove(0);
                if (current == null) {
                    dagPanel.add(depthPanel);
                    depthPanel = new JPanel();
                    depthPanel.setLayout(new GridLayout(0, 1, 0, 80));
                    if (queue.isEmpty()) {
                        break;
                    }
                    queue.add(null);
                    continue;
                }
                for (BuildStage child : current.getChildren()) {
                    if (!visited.contains(child.getName())) {
                        queue.add(child);
                        JButton button = addJButton(child.getName());
                        depthPanel.add(button);
                        visited.add(child.getName());
                    }
                }
            }
        }
        dagPanel.updateUI();
        dagPanel.validate();
    }

    private JButton addJButton(String name) {
        JButton button = new JButton(name);
        button.addActionListener(e -> updateStage(name));
        button.setForeground(JBColor.BLACK);
        button.setBackground(JBColor.GRAY);
        Border line = new LineBorder(JBColor.BLACK);
        Border margin = JBUI.Borders.empty(25, 15);
        Border compound = new CompoundBorder(line, margin);
        button.setBorder(compound);
        return button;
    }

    private boolean intersects(List<String> outs, List<String> deps) {
        for (String out : outs) {
            for (String dep : deps) {
                if (out.equals(dep)) {
                    return true;
                }
            }
        }
        return false;
    }

    public JPanel getContent() {
        return rootPanel;
    }

    private Pipeline parseYaml() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(project.getBasePath() + "/dvc.yaml");
            Map<String, Object> obj = yaml.load(inputStream);
            String[] temp = obj.get("stages").toString().split("},");
            Pipeline pipeline = new Pipeline();
            for (String stage : temp) {
                String stageName = stage.split("=")[0].substring(1);
                String cmd = stage.split("cmd=")[1].split(",")[0];
                List<String> deps = Arrays.stream(stage.split("deps=\\[")[1].split("]")[0].split(",")).map(String::trim).collect(Collectors.toList());
                List<String> outs = Arrays.stream(stage.split("outs=\\[")[1].split("]")[0].split(",")).map(String::trim).collect(Collectors.toList());
                pipeline.addStage(new BuildStage(stageName, cmd, deps, outs));
            }
            return pipeline;
        } catch (Exception e) {
            System.out.println("Can not Open DVC.yaml" + e.getMessage());
            Util.errorDialog("Can not Open DVC.yaml", "Can not Open DVC.yaml" + e.getMessage());
        }
        return null;
    }

    private void createUIComponents() {
        this.dagPanel = new DagPanel();
    }

    private void getPipeline(String yamlFile) {
        Yaml DVCYaml = new Yaml();
        File initialFile = new File(yamlFile);
        try {
            InputStream targetStream = new FileInputStream(initialFile);
            pipeLine = DVCYaml.load(targetStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void repro() {
        dagPanel.startPipeline();
        System.out.println("Starting Reproduction");
        System.out.println("active button: " + activeStage);
        if (pipeLine != null) {
            if (pipeLine.containsKey("stages")) {
                String dvcListCommand = "dvc repro " + activeStage; //repro only ./dvc.yaml to avoid deeper nested files
                Vector<String> outputList = new Vector<>();
                try {
                    String response = Util.runConsoleCommand(dvcListCommand, project.getBasePath(), new ProcessAdapter() {
                        @Override
                        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                            super.onTextAvailable(event, outputType);
                            String commandOutput = event.getText();
                            JLabel stageOutput = new JLabel();
                            stageOutput.setText(commandOutput);
                            System.out.println(commandOutput);
                            handleDagRainbow(commandOutput);
                            stageList.setListData(outputList);
                        }

                        @Override
                        public void processTerminated(@NotNull ProcessEvent event) {
                            dagPanel.finishLastStage();
                        }
                    });

                    if (!Util.commandRanCorrectly(response)) {
                        System.out.println("Failed Reproduction");
                        Util.errorDialog("Failed to Reproduce Pipeline", response);
                    } else {
                        System.out.println("Succeeded Reproduction");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            Util.errorDialog("No Pipeline Setup", "There is no pipeline detected. There should be a .yaml file in the DVC project.");
        }
    }

    private void handleDagRainbow(String commandOutput) {
        if (commandOutput.contains("Running stage '")) {
            String stageName = commandOutput.split("(?=('[^']*)')")[1].substring(1).split("':")[0];
            dagPanel.runStage(stageName);
        } else if (commandOutput.contains("Stage '")) {
            String stageName = commandOutput.split("(?=('[^']*)')")[1].substring(1);
            if (commandOutput.contains("didn't change, skipping")) {
                dagPanel.skipStage(stageName);
            } else if (commandOutput.contains("is cached - skipping run")) {
                dagPanel.cacheStage(stageName);
            }
        }
    }

}

class Pipeline {
    private List<BuildStage> stages;

    public List<BuildStage> getStages() {
        return stages;
    }

    public void addStage(BuildStage stage) {
        if (stages == null) {
            stages = new ArrayList<>();
        }
        stages.add(stage);
    }
}

class BuildStage {
    private final String name;
    private final String cmd;
    private final List<String> deps;
    private final List<String> outs;

    private final List<BuildStage> children = new ArrayList<>();

    public BuildStage(String name, String cmd, List<String> deps, List<String> outs) {
        this.name = name;
        this.cmd = cmd;
        this.deps = deps;
        this.outs = outs;
    }

    public String getName() {
        return name;
    }

    public String getCmd() {
        return cmd;
    }

    public List<String> getDeps() {
        return deps;
    }

    public List<String> getOuts() {
        return outs;
    }

    public void addChild(BuildStage stage) {
        children.add(stage);
    }

    public List<BuildStage> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "BuildStage{" +
                "name='" + name + '\'' +
                ", cmd='" + cmd + '\'' +
                ", deps=" + deps +
                ", outs=" + outs +
                ", children=" + children.stream().map(BuildStage::getName).collect(Collectors.joining(", ")) +
                '}';
    }
}

