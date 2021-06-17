package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class DVCDag {
    private JPanel rootPanel;
    private JPanel dagPanel;
    private final Project project;
    private final Pipeline pipeline;

    private JButton reproButton;
    private JList stageList;
    private String activeStage = "";

    private HashMap<String, Object> pipeLine;

    public DVCDag(@NotNull Project newProject) {
        project = newProject;
        pipeline = parseYaml();
        if (this.pipeline != null) {
            drawDag();
        }
        reproButton.addActionListener(e -> repro());
        getPipeline(project.getBasePath() + "/dvc.yaml");
    }

    private void updateStage(String stageName){
        activeStage = stageName;
        System.out.println("setting: " + stageName);
    }

    private void drawDag() {
        dagPanel.setLayout(new FlowLayout());
        BuildStage current = null;
        for (BuildStage stage : pipeline.getStages()) {
            if (stage.getDeps().size() == 1) {
                current = stage;
                break;
            }
        }
        final String pipelineStage = current.getName();
        JButton newButton = new JButton(pipelineStage);
        newButton.addActionListener(e -> updateStage(pipelineStage));
        dagPanel.add(newButton);
        while (true) {
            boolean hasNext = false;
            for (BuildStage next : pipeline.getStages()) {
                if (!current.equals(next) && intersects(current.getOuts(), next.getDeps())) {
                    dagPanel.add(new JLabel("-->"));
                    final String pipelineStage2 = next.getName();
                    newButton = new JButton(pipelineStage2);
                    newButton.addActionListener(e -> updateStage(pipelineStage2));
                    dagPanel.add(newButton);
                    current = next;
                    hasNext = true;
                    break;
                }
            }
            if (!hasNext) {
                break;
            }
        }
        dagPanel.updateUI();
    }

    private boolean intersects(List<String> outs, List<String> deps) {
        for (String out : outs) {
            for (String dep : deps) {
                if (out.equals(dep)) {
                    return true;
                }
            }
        }
        System.out.println(outs);
        System.out.println(deps);
        return false;
    }

    public JPanel getContent() {
        return rootPanel;
    }

    private Pipeline parseYaml() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(new File(project.getBasePath() + "/dvc.yaml"));
            Map<String, Object> obj = yaml.load(inputStream);
            String[] temp = obj.get("stages").toString().split("},");
            Pipeline pipeline = new Pipeline();
            for (String stage : temp) {
                String stageName = stage.split("=")[0].substring(1);
                String cmd = stage.split("cmd=")[1].split(",")[0];
                List<String> deps = Arrays.asList(stage.split("deps=\\[")[1].split("]")[0].split(","));
                List<String> outs = Arrays.asList(stage.split("outs=\\[")[1].split("]")[0].split(","));
                System.out.println(stageName);
                pipeline.addStage(new BuildStage(stageName, cmd, deps, outs));
            }
            return pipeline;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("COULDT OPEN DVC YAML");
        }
        return null;
    }

    private void getPipeline(String yamlFile){
        Yaml DVCYaml = new Yaml();
        File initialFile = new File(yamlFile);
        try{
            InputStream targetStream = new FileInputStream(initialFile);
            pipeLine = DVCYaml.load(targetStream);
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    private void repro() {
        System.out.println("Starting Reproduction");
        System.out.println("active button: " + activeStage);
        if (pipeLine != null) {
            if(pipeLine.containsKey("stages")){
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
                            outputList.add(commandOutput);
                            stageList.setListData(outputList);
                        }
                    });

                    if (!Util.commandRanCorrectly(response)) {
                        System.out.println("Failed Reproduction");
                        Util.errorDialog("Failed to Reproduce Pipeline", response);
                    } else {
                        System.out.println("Succeeded Reproduction");
                    }
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        } else {
            Util.errorDialog("No Pipeline Setup","There is no pipeline detected. There should be a .yaml file in the DVC project.");
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
}
