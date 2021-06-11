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
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Util {
    public static final String COMMAND_RAN_CORRECTLY = "";

    public static String runConsoleCommand(String command, String workingDirectory, ProcessAdapter processAdapter) {
        try {
            ArrayList<String> cmds = new ArrayList<>(Arrays.asList(command.split(" ")));

            GeneralCommandLine generalCommandLine = new GeneralCommandLine(cmds);
            generalCommandLine.setCharset(StandardCharsets.UTF_8);
            generalCommandLine.setWorkDirectory(workingDirectory);

            ProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
            processHandler.startNotify();
            processHandler.addProcessListener(processAdapter);
            return COMMAND_RAN_CORRECTLY;

        } catch (ExecutionException executionException) {
            String errorMessage = executionException.getMessage();
            System.out.println(errorMessage);
            return errorMessage;
        }
    }

    /**
     *
     * @param basePath project basepath
     * @return true if .dvc dir exists
     */
    public static boolean isDvcInProject(String basePath) {
        //Check for .dvc file in the project
        return Files.isDirectory(Paths.get(basePath + "/.dvc"));
    }

    public static Project getProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }

    public static boolean commandRanCorrectly(String message) {
        return message.equals(COMMAND_RAN_CORRECTLY);
    }
}
