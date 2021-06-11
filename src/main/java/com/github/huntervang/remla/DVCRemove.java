package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Objects;
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
                // TODO: provide user feedback when add has not been succesful
                System.err.println(message);
                System.err.println("on file " + filePath);
            }
        }
        //dvcList.refresh();
    }

    /*
    public static void openDvcRemoveFilePicker(Project project) {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false,
                true, true, false, true);
        fileChooserDescriptor.setRoots(ProjectRootManager.getInstance(project).getContentRootsFromAllModules());
        fileChooserDescriptor.setTitle("Choose Files/Folders to Remove from Dvc");
        fileChooserDescriptor.withFileFilter(virtualFile -> virtualFile.getPath().endsWith( ".dvc"));

        VirtualFile[] files = FileChooser.chooseFiles(fileChooserDescriptor, project, null);
        for (VirtualFile file : files) {
            dvcRemove(file, project);
        }
    }
    */
}
