package com.github.huntervang.remla;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessHandler;
import java.nio.charset.StandardCharsets;
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

    public static boolean commandRanCorrectly(String message) {
        return message.equals(COMMAND_RAN_CORRECTLY);
    }
}
