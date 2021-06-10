package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

public class DVCAddRemote {
    private JPanel dvcAddRemoteContent;
    private JButton addStorageButton;
    private JRadioButton localDirectoryRadioButton;
    private JRadioButton sshRadioButton;
    private JRadioButton googleDriveRadioButton;
    private JButton openCurrentStorageButton;
    private final Project project;

    public DVCAddRemote(ToolWindow toolWindow) {
        project = Util.getProject();
        updateOpenStorageButton();
    }

    private void updateOpenStorageButton() {
        String storageLocation = getExistingRemote();
        if (storageLocation != null) {
            openCurrentStorageButton.setVisible(true);
            addStorageButton.setText("Choose new storage location");
            addStorageButton.addActionListener(e -> chooseStorageLocation(true));
            openCurrentStorageButton.setText("Current location: " + storageLocation);
            openCurrentStorageButton.addActionListener(e -> RevealFileAction.openDirectory(new File(storageLocation)));
        } else {
            addStorageButton.setText("Choose storage location");
            addStorageButton.addActionListener(e -> chooseStorageLocation(false));
            openCurrentStorageButton.setVisible(false);
        }
    }

    private String getExistingRemote() {
        try {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + "/.dvc/config");
            if (file != null) {
                CharSequence fileContent = LoadTextUtil.loadText(file);
                String[] lines = fileContent.toString().split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].contains("['remote ")) {
                        return lines[i + 1].split("url = ")[1];
                    }
                }
            }
        } catch (NullPointerException e) {
            return null;
        }
        return null;
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
        }
    }

    private void addLocalStorage(VirtualFile dir, boolean force) {
        String response = Util.runConsoleCommand("dvc remote add data-storage " + dir.getCanonicalPath() + (force ? " --force" : ""), project.getBasePath(), new ProcessAdapter() {
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
