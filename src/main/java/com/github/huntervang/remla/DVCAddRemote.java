package com.github.huntervang.remla;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class DVCAddRemote {
    private JPanel dvcAddRemoteContent;
    private JTextField remoteInput;
    private JButton addStorageButton;
    private final Project project;

    public DVCAddRemote(ToolWindow toolWindow) {
        project = ProjectManager.getInstance().getOpenProjects()[0];
        addStorageButton.addActionListener(e -> dvcAddRemote());
    }

    public JPanel getContent() {
        return dvcAddRemoteContent;
    }

    public void dvcAddRemote() {
        runConsoleCommand("dvc remote add myremote " + remoteInput.getText(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                String commandOutput = event.getText();
                System.out.println(commandOutput);
            }
        });
    }

    private void runConsoleCommand(String command, ProcessAdapter processAdapter) {
        try {
            ArrayList<String> cmds = new ArrayList<>(Arrays.asList(command.split(" ")));

            GeneralCommandLine generalCommandLine = new GeneralCommandLine(cmds);
            generalCommandLine.setCharset(StandardCharsets.UTF_8);
            generalCommandLine.setWorkDirectory(project.getBasePath());

            ProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
            processHandler.startNotify();
            processHandler.addProcessListener(processAdapter);

        } catch (ExecutionException executionException) {
            System.out.println(executionException.getMessage());
        }
    }
}
