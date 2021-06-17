package com.github.huntervang.remla;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DVCAddAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null) {
            e.getPresentation().setText(file.isDirectory() ? "Add Folder" : "Add File");
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project != null && file != null) {
            int result = Messages.showOkCancelDialog(project, "Would you like to add " + file + " to DVC?", "Confirmation", "Ok", "Cancel", Messages.getInformationIcon());

            if (result == Messages.OK) {
                DVCAdd.dvcAdd(file, project);
            }
        } else {
            // TODO: some error feedback
        }
    }


}
