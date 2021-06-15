package com.github.huntervang.remla;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

import static com.github.huntervang.remla.DVCAdd.openDvcAddFilePicker;

public class DVCToolWindow {
    private JPanel dvcToolWindowContent;
    private JPanel addRemote;
    private JPanel filePanel;
    private JButton addButton;
    Project project;
    ToolWindow toolWindow;

    public DVCToolWindow(Project newproject, ToolWindow newtoolWindow) {
        project = newproject;
        toolWindow =newtoolWindow;
        addButton.addActionListener(e -> openDvcAddFilePicker(project));

    }

    public JPanel getContent() {
        return dvcToolWindowContent;
    }

    private void createUIComponents() {
        addRemote = (new DVCAddRemote(toolWindow, project)).getContent();
        filePanel = (new MakeDVCList(project)).getContent();
    }
}
