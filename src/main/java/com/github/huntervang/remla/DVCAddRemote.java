package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DVCAddRemote {
    private JPanel dvcAddRemoteContent;
    private JTextField remoteInput;
    private JButton addStorageButton;

    public DVCAddRemote(ToolWindow toolWindow) {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        addStorageButton.addActionListener(e -> dvcAddRemote());
    }

    public JPanel getContent() {
        return dvcAddRemoteContent;
    }

    public Content getToolWindowContent() {
        return ContentFactory.SERVICE.getInstance().createContent(dvcAddRemoteContent, "", false);
    }

    public void dvcAddRemote() {
        String response = Util.runConsoleCommand("dvc remote add myremote " + remoteInput.getText(),".", new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                String commandOutput = event.getText();
            }
        });
    }
}
