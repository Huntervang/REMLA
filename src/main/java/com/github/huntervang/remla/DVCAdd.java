package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DVCAdd {
    public static void dvcAdd(VirtualFile file, Project project) {
        String message = Util.runConsoleCommand("dvc add " + file.getPath(), project.getBasePath(), new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                // TODO: update UI when new files are dvc added
            }
        });

        if (!Util.commandRanCorrectly(message)) {
            System.err.println(message);
            System.err.println("on file " + file.getPath());
            Util.errorDialog("Error: Add file to DVC failed", message);
        }
    }

    public static void openDvcAddFilePicker(Project project) {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true,
                true, true, false, true);
        fileChooserDescriptor.setRoots(ProjectRootManager.getInstance(project).getContentRootsFromAllModules());
        fileChooserDescriptor.withFileFilter(virtualFile -> {
            boolean noDvcFileExists = virtualFile.getParent().findChild(virtualFile.getName() + ".dvc") == null;
            boolean isNotADVCFile = !virtualFile.getPath().endsWith(".dvc");
            return isNotADVCFile && noDvcFileExists;
        });
        fileChooserDescriptor.setTitle("Choose Files/Folders to Add to Dvc");

        VirtualFile[] files = FileChooser.chooseFiles(fileChooserDescriptor, project, null);
        for (VirtualFile file : files) {
            dvcAdd(file, project);
        }
    }
}
