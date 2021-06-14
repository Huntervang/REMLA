package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DVCRepro {
    private JScrollPane scrollPane;
    private JPanel reproPanel;
    private JButton repro;
    private final Project project;

    public DVCRepro(Project thisProject){
        project = thisProject;
        assert repro != null;
        repro.addActionListener(e -> repro());
    }

    private void repro() {
        System.out.println("Starting Reproduction");
        if (true) { //TODO check for YAML file
            String dvcListCommand = "dvc repro";
            try {
                String response = Util.runConsoleCommand(dvcListCommand, project.getBasePath(), new ProcessAdapter() {
                    @Override
                    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                        super.onTextAvailable(event, outputType);
                        String commandOutput = event.getText();
                        JLabel stageOutput = new JLabel();
                        stageOutput.setText(commandOutput);
                        scrollPane.add(stageOutput);
                        System.out.println("Adding stage with string:");
                        System.out.println(commandOutput);
                        scrollPane.repaint();
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
