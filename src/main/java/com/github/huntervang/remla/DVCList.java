package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.JBColor;

import java.awt.Font;
import java.awt.Insets;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class DVCList {
    private JPanel filePanel;
    private JLabel fileLabel;
    private JButton pushButton;
    private CheckBoxList<CheckListItem> checkBoxList;
    private final Project project;

    private final HashMap<String, Color> localColorMap = new HashMap<String, Color>() {{
        put("new" , JBColor.GREEN);             //new: An output is found in the workspace, but there is no corresponding file hash saved in the dvc.lock or .dvc file yet.
        put("modified" , JBColor.BLUE);         //modified: An output or dependency is found in the workspace, but the corresponding file hash in the dvc.lock or .dvc file is not up to date.
        put("deleted" , JBColor.GRAY);          //deleted: The output or dependency is referenced in a dvc.lock or .dvc file, but does not exist in the workspace.
        put("not in cache" , JBColor.RED); }};  //not in cache: An output exists in the workspace, and the corresponding file hash in the dvc.lock or .dvc file is up to date, but there is no corresponding cache file or directory.

    private final HashMap<String, Color> remoteColorMap = new HashMap<String, Color>() {{
        put("new" , JBColor.GREEN);         //new means that the file/directory exists in the cache but not in remote storage.
        put("deleted" , JBColor.GRAY);      //deleted means that the file/directory doesn't exist in the cache, but exists in remote storage.
        put("missing" , JBColor.RED); }};   //missing means that the file/directory doesn't exist neither in cache, nor in remote storage.

    private final HashMap<String, String> dvcStatus = new HashMap<>();
    private final HashMap<String, String> dvcStatusRemote = new HashMap<>();

    public DVCList(Project thisProject){
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

    private void refresh(){
        runDVCStatusAndList();
    }

    private void push() {
        if (Util.getExistingRemote() != null) {
            for (int i = 0; i < checkBoxList.getModel().getSize(); i++) { //iterate through file list, push when checked
                CheckListItem item = checkBoxList.getItemAt(i);
                if (checkBoxList.isItemSelected(i) && item != null) { //check if file is checked
                    String filename = item.toString();
                    String dvcListCommand = "dvc push " + filename;
                    String response = Util.runConsoleCommand(dvcListCommand, ".", new ProcessAdapter() {
                        @Override
                        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                            super.onTextAvailable(event, outputType);
                            try {
                                System.out.println(event.getText());
                                //TODO parse command response
                            } catch (org.json.JSONException e) {
                                //TODO on command failure
                            }
                        }
                    });

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
        setDVCStatus(this::setDVCFileList, false);

        //TODO setDVCStatus(true) to set for status wrt remote

    }

    private void setDVCFileList() {
        String dvcListCommand = "dvc list . -R --dvc-only --show-json"; //TODO handle folder path
        String response = Util.runConsoleCommand(dvcListCommand, project.getBasePath(), new ProcessAdapter() {
            JSONArray status;
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    status = new JSONArray(commandOutput);
                }
                catch(org.json.JSONException e){
                    status = new JSONArray();
                }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);

                checkBoxList.clear();

                Vector<String> checkedFiles = new Vector<>();
                for(int i=0; i< checkBoxList.getModel().getSize(); i++){
                    JCheckBox checkBox = checkBoxList.getModel().getElementAt(i);
                    if (checkBox.isSelected()) {
                        checkedFiles.add(checkBox.toString());
                    }
                }

                if (status.length() == 0){ //emtpy file list
                    fileLabel.setText("There are no files tracked yet");
                    return;
                }

                fileLabel.setText("Your tracked files:");

                for (Object o : status) {
                    JSONObject file = (JSONObject) o;
                    String fileName = file.getString("path");

                    Color fileColor;
                    if (dvcStatus.containsKey(fileName)) {
                        String fileStatus = dvcStatus.get(fileName);
                        fileColor = localColorMap.get(fileStatus);
                    } else {
                        fileColor = JBColor.GREEN;
                    }

                    CheckListItem checkListItem = new CheckListItem(fileName, fileColor);
                    checkBoxList.addItem(checkListItem, fileName, checkedFiles.contains(fileName));
                }
            }
        });

        if(!Util.commandRanCorrectly(response)){
            fileLabel.setText(response);
        }
    }

    private void setDVCStatus(Runnable runnable, boolean remote) {
        String dvcStatusCommand;
        if(remote)
            dvcStatusCommand = "dvc status --remote --show-json";
        else
            dvcStatusCommand = "dvc status --show-json";

        String response = Util.runConsoleCommand(dvcStatusCommand, project.getBasePath(), new ProcessAdapter() {
            JSONObject status;
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    status = new JSONObject(commandOutput);
                    for(String dvcFile : status.keySet()) { //traverse json format output of dvc status (entry per *.dvc file)
                        JSONArray DVCFileStatus = (JSONArray) status.get(dvcFile);
                        for(Object statusEntries : DVCFileStatus) {
                            JSONObject statusjson = (JSONObject) statusEntries;
                            JSONObject outgoingEntry = (JSONObject) statusjson.get("changed outs");
                            for(String filename : outgoingEntry.keySet()){
                                if(remote)
                                    dvcStatusRemote.put(filename, (String) outgoingEntry.get(filename));
                                else
                                    dvcStatus.put(filename, (String) outgoingEntry.get(filename));
                            }
                        }//TODO incoming EntrySet?
                    }
                    System.out.println(dvcStatus.keySet());
                }
                catch(org.json.JSONException e){
                    status = new JSONObject();
                }
            }
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                runnable.run();
            }
        });
        if(!Util.commandRanCorrectly(response)){
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
