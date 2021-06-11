package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EventListener;

public class DVCAddRemote {
    private JPanel dvcAddRemoteContent;
    private JButton addStorageButton;
    private JRadioButton localDirectoryRadioButton;
    private JRadioButton sshRadioButton;
    private JRadioButton googleDriveRadioButton;
    private JButton openCurrentStorageButton;
    private final Project project;
    private Icon defaultIcon;

    public DVCAddRemote(ToolWindow toolWindow) {
        project = Util.getProject();
        defaultIcon = openCurrentStorageButton.getIcon();
        updateOpenStorageButton();
    }

    private void updateOpenStorageButton() {
        String storageLocation = Util.getExistingRemote();
        removeActionListeners(addStorageButton);
        removeActionListeners(openCurrentStorageButton);
        if (storageLocation != null) {
            addStorageButton.setText("Choose new storage location");
            addStorageButton.addActionListener(e -> chooseStorageLocation(true));
            openCurrentStorageButton.setVisible(true);
            openCurrentStorageButton.setText("Current location: " + storageLocation);
            if (storageLocation.contains("gdrive")) {
                openCurrentStorageButton.addActionListener(e -> BrowserUtil.browse("https://drive.google.com/drive/u/0/folders/" + storageLocation.split("gdrive://")[1]));
                openCurrentStorageButton.setIcon(IconLoader.getIcon("/icons/gdrive_16x16.png"));
            } else {
                openCurrentStorageButton.addActionListener(e -> RevealFileAction.openDirectory(new File(storageLocation)));
                openCurrentStorageButton.setIcon(defaultIcon);
            }
        } else {
            addStorageButton.setText("Choose storage location");
            addStorageButton.addActionListener(e -> chooseStorageLocation(false));
            openCurrentStorageButton.setVisible(false);
        }
    }

    private void removeActionListeners(JButton button) {
        for (ActionListener actionListener : button.getActionListeners()) {
            button.removeActionListener(actionListener);
        }
    }

    public JPanel getContent() {
        return dvcAddRemoteContent;
    }

    public void chooseStorageLocation(boolean force) {
        if (localDirectoryRadioButton.isSelected()) {
            System.out.println("Local storage!");
            VirtualFile dir = chooseLocalDirectory();
            addLocalStorage(dir, force);
        } else if (sshRadioButton.isSelected()) {
            System.out.println("SSH storage!");
        } else if (googleDriveRadioButton.isSelected()) {
            System.out.println("Google Drive storage!");
            String input = Messages.showInputDialog(project, "Please the ID of your Google Drive folder", "Enter Google Drive ID", Messages.getQuestionIcon());
            if (input != null) {
                addGDriveStorage(input, force);
            }
        }
    }

    private void addGDriveStorage(String gDriveId, boolean force) {
        String response = Util.runConsoleCommand("dvc remote add --default data-storage gdrive://" + gDriveId + (force ? " --force" : ""), project.getBasePath(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                String commandOutput = event.getText();
                System.out.println(commandOutput);
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                System.out.println("Command done!");
                updateOpenStorageButton();
            }
        });
    }

    private void addLocalStorage(VirtualFile dir, boolean force) {
        String response = Util.runConsoleCommand("dvc remote add --default data-storage " + dir.getCanonicalPath() + (force ? " --force" : ""), project.getBasePath(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                String commandOutput = event.getText();
                System.out.println(commandOutput);
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                System.out.println("Command done!");
                updateOpenStorageButton();
            }
        });
    }

    public VirtualFile chooseLocalDirectory() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true,
                false, false, false, false);
        fileChooserDescriptor.setTitle("Choose a Folder to Store Datafiles Managed by DVC");
        return FileChooser.chooseFile(fileChooserDescriptor, project, null);
    }
}
