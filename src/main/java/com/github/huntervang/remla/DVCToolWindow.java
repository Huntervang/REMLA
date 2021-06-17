package com.github.huntervang.remla;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

import static com.github.huntervang.remla.DVCAdd.openDvcAddFilePicker;
import static com.github.huntervang.remla.DVCRemove.dvcRemove;

public class DVCToolWindow {
    private JPanel dvcToolWindowContent;
    private JPanel addRemote;
    private JPanel filePanel;
    private JButton addButton;
    private JButton removeButton;
    private MakeDVCList dvcList;
    Project project;
    ToolWindow toolWindow;

    public DVCToolWindow(Project newproject, ToolWindow newtoolWindow) {
        project = newproject;
        toolWindow =newtoolWindow;
        addButton.addActionListener(e -> openDvcAddFilePicker(project));
        removeButton.addActionListener(e -> dvcRemove(project, dvcList));

    }

    public JPanel getContent() {
        return dvcToolWindowContent;
    }

    private void createUIComponents() {
        dvcList = new MakeDVCList(project);
        filePanel = dvcList.getContent();
        addRemote = (new DVCAddRemote(toolWindow)).getContent();
    }
}