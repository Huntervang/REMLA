package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class DVCRepro {
    private JScrollPane scrollPane;
    private JPanel reproPanel;
    private JButton repro;
    private JList<String> stageList;
    private final Project project;

    private HashMap<String, Object> pipeLine;

    public DVCRepro(Project thisProject){
        project = thisProject;
        assert repro != null;
        repro.addActionListener(e -> repro());
        getPipeline(thisProject.getBasePath() + "/dvc.yaml");
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
        if (pipeLine != null) {
            if(pipeLine.containsKey("stages")){
                System.out.println(pipeLine.get("stages")) ;
                //Set<String> stages = stagesMap.keySet();


                String dvcListCommand = "dvc repro dvc.yaml"; //repro only ./dvc.yaml to avoid deeper nested files
                Vector<String> outputList = new Vector<>();
                try {
                    String response = Util.runConsoleCommand(dvcListCommand, project.getBasePath(), new ProcessAdapter() {
                        @Override
                        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                            super.onTextAvailable(event, outputType);
                            String commandOutput = event.getText();
                            JLabel stageOutput = new JLabel();
                            stageOutput.setText(commandOutput);
                            scrollPane.add(stageOutput);
                            outputList.add(commandOutput);
                            System.out.println("Adding stage with string:");
                            System.out.println(commandOutput);
                            stageList.setListData(outputList);

                            //String stage = stages.firstKey();
                            //System.out.println(stage);

                            JTextArea stageText = new JTextArea();

                            stageText.append(commandOutput);
                            stageText.append("\n");
                            scrollPane.add(stageText);
                        }
                    });

                    if (!Util.commandRanCorrectly(response)) {
                        //todo repro command failed
                        System.out.println("Failed Reproduction");
                    } else {
                        System.out.println("Succeeded Reproduction");
                    }
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        } else {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(project,
                    "There is no pipeline detected. There should be a .yaml file in the DVC project.",
                    "No Pipeline Setup"
            ));
        }
    }

    public JPanel getContent() {
        return reproPanel;
    }

}
