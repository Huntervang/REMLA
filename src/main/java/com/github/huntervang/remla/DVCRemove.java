package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;

public class DVCRemove {
    public static void dvcRemove(Project project, MakeDVCList dvcList) {
        Vector<String> checkedFiles = dvcList.getCheckedFiles();
        for(String filePath : checkedFiles) {
            System.out.println(filePath);
            String dotDVCFilePath =  filePath + ".dvc";
            //TODO if exists?
            String message = Util.runConsoleCommand("dvc remove " + dotDVCFilePath, project.getBasePath(), new ProcessAdapter() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    super.processTerminated(event);
                    // TODO: update UI when new files are dvc added
                    System.out.println(filePath + " removed");
                }
            });

            if (!Util.commandRanCorrectly(message)) {
                System.err.println(message);
                System.err.println("on file " + filePath);
                Util.errorDialog("Error: Remove file from DVC failed", message);
            }
        }
    }
}
