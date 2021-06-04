package com.github.huntervang.remla;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HelloWorldAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (project == null) {
            return;
        }

        try {
            ArrayList<String> cmds = new ArrayList<>();
            cmds.add("git");
            cmds.add("status");

            GeneralCommandLine generalCommandLine = new GeneralCommandLine(cmds);
            generalCommandLine.setCharset(StandardCharsets.UTF_8);
            generalCommandLine.setWorkDirectory(project.getBasePath());

            ProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
            processHandler.startNotify();

            // Add event listener on text available of command output
            processHandler.addProcessListener(new ProcessAdapter() {
                final StringBuilder fullOutput = new StringBuilder();

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    super.onTextAvailable(event, outputType);
                    fullOutput.append(event.getText());
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    super.processTerminated(event);

                    Runnable displayHelloWorld = () -> {
                        Messages.showMessageDialog(project, fullOutput.toString(), "Command Output", Messages.getInformationIcon());
                    };

                    // Queue the showMessageDialog so it gets executed during the dispatch event
                    ApplicationManager.getApplication().invokeLater(displayHelloWorld);
                }
            });

        } catch (ExecutionException executionException) {
            System.out.println(executionException.getMessage());
        }

    }
}
