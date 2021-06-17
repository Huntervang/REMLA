package com.github.huntervang.remla;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.yaml.snakeyaml.Yaml;
import sun.tools.jps.Jps;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
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
    private DrawableJPanel dagPanel;
    private JButton runPipelineButton;
    private final Project project;
    private final Pipeline pipeline;

    public DVCDag(Project project) {
        this.project = project;
        this.pipeline = parseYaml();
        if (this.pipeline != null) {
            drawDag();
        }
    }

    private void drawDag() {
        dagPanel.setLayout(new FlowLayout());

        List<BuildStage> rootNodes = new ArrayList<>();
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
            depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.Y_AXIS));
            while (!queue.isEmpty()) {
                BuildStage current = queue.remove(0);
                if (current == null) {
                    dagPanel.add(depthPanel);
                    depthPanel = new JPanel();
                    depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.Y_AXIS));
                    if (queue.isEmpty()) {
                        break;
                    }
                    queue.add(null);
                    continue;
                }
                for (BuildStage child : current.getChildren()) {
                    queue.add(child);
                    JButton button = addJButton(child.getName());
                    depthPanel.add(button);
                }
            }
        }
        dagPanel.updateUI();
        dagPanel.validate();
    }

    private JButton addJButton(String name) {
        JButton button = new JButton(name);
        button.setForeground(JBColor.BLACK);
        button.setBackground(JBColor.GRAY);
//        button.setPreferredSize(new Dimension(200, 150));
        Border line = new LineBorder(JBColor.BLACK);
        Border margin = JBUI.Borders.empty(5, 15);
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
            InputStream inputStream = new FileInputStream(new File(project.getBasePath() + "/dvc.yaml"));
            Map<String, Object> obj = yaml.load(inputStream);
            String[] temp = obj.get("stages").toString().split("},");
            Pipeline pipeline = new Pipeline();
            for (String stage : temp) {
                String stageName = stage.split("=")[0].substring(1);
                String cmd = stage.split("cmd=")[1].split(",")[0];
                List<String> deps = Arrays.asList(stage.split("deps=\\[")[1].split("]")[0].split(","));
                List<String> outs = Arrays.asList(stage.split("outs=\\[")[1].split("]")[0].split(","));
                pipeline.addStage(new BuildStage(stageName, cmd, deps, outs));
            }
            return pipeline;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("COULDT OPEN DVC YAML");
        }
        return null;
    }

    private void createUIComponents() {
        this.dagPanel = new DrawableJPanel();
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

    private List<BuildStage> children = new ArrayList<>();

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
                ", children=" + children +
                '}';
    }
}

