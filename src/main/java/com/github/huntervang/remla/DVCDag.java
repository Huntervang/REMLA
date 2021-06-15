package com.github.huntervang.remla;

import com.intellij.openapi.project.Project;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DVCDag {
    private JPanel rootPanel;
    private final Project project;
    private final Pipeline pipeline;

    public DVCDag(Project project) {
        this.project = project;
        this.pipeline = parseYaml();
        drawDag();
    }

    private void drawDag() {
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));
        BuildStage current = null;
        for (BuildStage stage : pipeline.getStages()) {
            if (stage.getDeps().size() == 1) {
                current = stage;
                break;
            }
        }
        rootPanel.add(new JButton(current.getName()));
        while (true) {
            boolean hasNext = false;
            for (BuildStage next : pipeline.getStages()) {
                if (!current.equals(next) && intersects(current.getOuts(), next.getDeps())) {
                    rootPanel.add(new JLabel("-->"));
                    rootPanel.add(new JButton(next.getName()));
                    current = next;
                    hasNext = true;
                    break;
                }
            }
            if (!hasNext) {
                break;
            }
        }
        rootPanel.updateUI();
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
    private String name;
    private String cmd;
    private List<String> deps;
    private List<String> outs;

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
