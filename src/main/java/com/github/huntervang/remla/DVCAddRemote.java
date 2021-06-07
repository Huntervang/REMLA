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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;

import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

public class DVCAddRemote {
    private JPanel dvcAddRemoteContent;
    private JTextField remoteInput;
    private JButton addStorageButton;

    public DVCAddRemote(ToolWindow toolWindow) {
        project = ProjectManager.getInstance().getOpenProjects()[0];
        Project = project;
        addStorageButton.addActionListener(e -> dvcAddRemote());
        setDVCFileList(); //TODO set filelist is called in first render, should be applied on some trigger, but don't know which trigger
    }

    public JPanel getContent() {
        return dvcAddRemoteContent;
    }

    public void dvcAddRemote() {
        String response = Util.runConsoleCommand("dvc remote add myremote " + remoteInput.getText(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                String commandOutput = event.getText();
            }
        });
    }
}