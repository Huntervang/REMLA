package com.github.huntervang.remla;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class MakeDVCList {
    private JPanel filePanel;
    private JLabel fileLabel;
    private JButton pushButton;
    private CheckBoxList<CheckListItem> checkBoxList;
    private final Project project;

    private final HashMap<String, Color> colorMap = new HashMap<String, Color>() {{
        put("new", JBColor.GREEN);
        put("modified", JBColor.BLUE);
        put("deleted", JBColor.GRAY);
        put("not in cache", JBColor.RED);
    }};

    public MakeDVCList(Project thisProject) {
        project = thisProject;
        runDVCStatusAndList(); //TODO set filelist is called in first render,
        // should be applied on some trigger, but don't know which trigger
        pushButton.addActionListener(e -> push());

        checkBoxList.setCellRenderer(new ColoredListRenderer());
        checkBoxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                refresh();
            }
        });
    }

    private void refresh() {
        runDVCStatusAndList();
    }

    private void showGoogleDriveDialog(String url, OSProcessHandler processHandler) {
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("Enter Authentication Code");
        builder.removeAllActions();
        JTextField inputField = new JTextField();
        builder.setCenterPanel(inputField);
        builder.addAction(new AbstractAction("Submit code") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    processHandler.getProcessInput().write((inputField.getText()).getBytes());
                    processHandler.getProcessInput().close();
                    builder.getDialogWrapper().close(0);

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        builder.addAction(new AbstractAction("Get authentication code") {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse(url);
            }
        });
        builder.show();
    }

    private void push() {
        String remote = Util.getExistingRemote();
        if (remote != null) {
            for (int i = 0; i < checkBoxList.getModel().getSize(); i++) { //iterate through file list, push when checked
                CheckListItem item = checkBoxList.getItemAt(i);
                if (checkBoxList.isItemSelected(i) && item != null) { //check if file is checked
                    String filename = item.toString();
                    String dvcListCommand = "dvc push " + filename;
                    ArrayList<String> cmds = new ArrayList<>(Arrays.asList(dvcListCommand.split(" ")));
                    GeneralCommandLine generalCommandLine = new GeneralCommandLine(cmds);
                    generalCommandLine.setCharset(StandardCharsets.UTF_8);
                    generalCommandLine.setWorkDirectory(project.getBasePath());
                    String response = null;
                    try {
                        OSProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
                        response = Util.runConsoleCommand(processHandler, new ProcessAdapter() {
                            @Override
                            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                                super.onTextAvailable(event, outputType);
                                try {
                                    if (remote.contains("gdrive") && event.getText().contains("https://")) {
                                        ApplicationManager.getApplication().invokeLater(() -> showGoogleDriveDialog(event.getText(), processHandler));
                                    }
                                    System.out.println(event.getText());
                                    //TODO parse command response
                                } catch (org.json.JSONException e) {
                                    //TODO on command failure
                                }
                            }
                        });
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    //TODO based on response: provide feedback
                    if (Util.commandRanCorrectly(response)) {
                        System.out.println("command executed properly i guess");
                    }
                }
            }
        } else {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(project,
                    "You have not selected a storage location yet, please choose one using the menu on the right",
                    "No Storage Location Selected"
            ));
        }
    }

    private void runDVCStatusAndList() {
        setDVCFileList();
        setDVCStatus();
    }

    public Vector<String> getCheckedFiles() {
        Vector<String> checkedFiles = new Vector<>();
        for(int i=0; i< checkBoxList.getModel().getSize(); i++){
            JCheckBox checkBox = checkBoxList.getModel().getElementAt(i);
            if (checkBox.isSelected()) {
                checkedFiles.add(checkBox.getText());
            }
        }
        return checkedFiles;
    }

    public Map<String, Color> getFileColors() {
        Map<String, Color> fileColors = new HashMap<>();
        for (int i = 0; i < checkBoxList.getItemsCount(); i++) {
            CheckListItem itemAt = checkBoxList.getItemAt(i);
            if (itemAt == null) {
                continue;
            }

            fileColors.put(itemAt.label, itemAt.color);
        }
        return fileColors;
    }

    private void setDVCFileList() {
        String dvcListCommand = "dvc list . -R --dvc-only --show-json"; //TODO handle folder path
        String response = Util.runConsoleCommand(dvcListCommand, project.getBasePath(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    JSONArray status = new JSONArray(commandOutput);

                    if (status.length() == 0) { //emtpy file list
                        fileLabel.setText("There are no files tracked yet");
                        return;
                    }

                    Vector<String> checkedFiles = getCheckedFiles();
                    Map<String, Color> fileColors = getFileColors();

                    fileLabel.setText("Your tracked files:");

                    ArrayList<Pair<CheckListItem, Boolean>> toAdd = new ArrayList<>();
                    for (Object o : status) {
                        JSONObject file = (JSONObject) o;
                        String fileName = file.getString("path");

                        CheckListItem checkListItem = new CheckListItem(fileName,
                                fileColors.getOrDefault(fileName, JBColor.GREEN));

                        toAdd.add(new Pair<>(checkListItem, checkedFiles.contains(fileName)));
                    }

                    // Sometimes clear would be visible, showing an empty file list
                    // That is why the toAdd array was added
                    checkBoxList.clear();
                    for (Pair<CheckListItem, Boolean> pair : toAdd) {
                        checkBoxList.addItem(pair.first, pair.first.label, pair.second);
                    }

                } catch (org.json.JSONException e) {
                    // TODO: show error
                }
            }
        });

        if (!Util.commandRanCorrectly(response)) {
            fileLabel.setText(response);
        }
    }

    private void setDVCStatus() {
        String dvcStatusCommand = "dvc status --show-json";
        String response = Util.runConsoleCommand(dvcStatusCommand, project.getBasePath(), new ProcessAdapter() {

            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    JSONObject status = new JSONObject(commandOutput);
                    for (String dvcFile : status.keySet()) { //traverse json format output of dvc status (entry per *.dvc file)
                        JSONArray DVCFileStatus = (JSONArray) status.get(dvcFile);

                        for (Object statusEntries : DVCFileStatus) {
                            JSONObject statusjson = (JSONObject) statusEntries;
                            JSONObject outgoingEntry = (JSONObject) statusjson.get("changed outs");

                            for (String filename : outgoingEntry.keySet()) {
                                for (int i = 0; i < checkBoxList.getItemsCount(); i++) {
                                    CheckListItem itemAt = checkBoxList.getItemAt(i);
                                    if (itemAt == null) {
                                        continue;
                                    }

                                    if (filename.equals(itemAt.label)) {
                                        String fileStatus = (String) outgoingEntry.get(filename);
                                        Color fileColor = colorMap.get(fileStatus);

                                        CheckListItem newItem = new CheckListItem(itemAt.label, fileColor);
                                        checkBoxList.updateItem(itemAt, newItem, itemAt.label);
                                    }
                                }
                            }
                        }
                    }
                } catch (org.json.JSONException e) {
                    // TODO: show some error
                }
            }
        });
        if (!Util.commandRanCorrectly(response)) {
            fileLabel.setText(response);
        }
    }

    public JPanel getContent() {
        return filePanel;
    }

    private static class CheckListItem {
        private final String label;
        private final Color color;

        public CheckListItem(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public Color color() {
            return this.color;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckListItem that = (CheckListItem) o;
            return Objects.equals(label, that.label) && Objects.equals(color, that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label, color);
        }
    }

    private class ColoredListRenderer implements ListCellRenderer<JCheckBox> {
        private final Border mySelectedBorder;
        private final Border myBorder;

        private ColoredListRenderer() {
            mySelectedBorder = UIManager.getBorder("List.focusCellHighlightBorder");
            Insets myBorderInsets = mySelectedBorder.getBorderInsets(new JCheckBox());
            myBorder = new EmptyBorder(myBorderInsets);
        }

        @Override
        public Component getListCellRendererComponent(JList list, JCheckBox checkbox, int index, boolean isSelected, boolean cellHasFocus) {
            Color textColor;

            CheckListItem checkListItem = checkBoxList.getItemAt(index);
            if (checkListItem != null) {
                textColor = checkListItem.color();
            } else {
                textColor = list.getForeground();
            }

            Color backgroundColor = isSelected ? list.getSelectionBackground() : list.getBackground();

            Font font = checkBoxList.getFont();
            checkbox.setBackground(backgroundColor);
            checkbox.setForeground(textColor);
            checkbox.setEnabled(list.isEnabled());
            checkbox.setFont(font);
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(false);
            checkbox.setOpaque(true);

            checkbox.setBorder(isSelected ? mySelectedBorder : myBorder);

            boolean isRollOver = checkbox.getModel().isRollover();
            checkbox.getModel().setRollover(isRollOver);

            return checkbox;
        }
    }
}
