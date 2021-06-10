package com.github.huntervang.remla;

import static com.github.huntervang.remla.DVCAdd.openDvcAddFilePicker;
import com.github.huntervang.remla.MakeDVCList;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class DVCToolWindow {
    private JButton addButton;
    private MakeDVCList fileList;
    private DVCAddRemote remoteDialog;
    private JPanel dvcToolWindowContent;

    public DVCToolWindow(Project project, ToolWindow toolWindow) {
        //addButton.addActionListener(e -> openDvcAddFilePicker(project));
        dvcToolWindowContent.add(fileList.getContent());
        dvcToolWindowContent.add(remoteDialog.getContent());
    }

    public JPanel getContent() {
        return dvcToolWindowContent;
    }

}