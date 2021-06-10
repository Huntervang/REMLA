package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class MakeDVCList {
    private JPanel filePanel;
    private JLabel fileLabel;
    private JList<CheckListItem> fileList;
    private JButton pushButton;
    private final Project project;

    private final HashMap<String, Color> colorMap = new HashMap<String, Color>() {{
        put("new" , JBColor.GREEN);
        put("modified" , JBColor.BLUE);
        put("deleted" , JBColor.GRAY);
        put("not in cache" , JBColor.RED); }};

    private boolean waitingForStatus = true;
    private final HashMap<String, String> dvcStatus = new HashMap<>();

    public MakeDVCList(Project thisProject){
        project = thisProject;
        setDVCFileList(); //TODO set filelist is called in first render,
                          // should be applied on some trigger, but don't know which trigger
        pushButton.addActionListener(e -> push());

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                refresh();
            }
        });
    }

    public void refresh(){
        setDVCFileList();
    }

    private void push() {
        for(int i=0; i<fileList.getModel().getSize(); i++ ){ //iterate through file list, push when checked
            CheckListItem item = (CheckListItem) fileList.getModel().getElementAt(i);
            if(item.isSelected()){ //check if file is checked
                String filename = item.toString();
                String dvcListCommand = "dvc push " + filename;
                String response = Util.runConsoleCommand(dvcListCommand,".", new ProcessAdapter() {
                    @Override
                    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                        super.onTextAvailable(event, outputType);
                        try {
                            System.out.println(event.getText());
                            //TODO parse command response
                        }
                        catch(org.json.JSONException e){
                            //TODO on command failure
                        }
                    }
                });

                //TODO based on response: provide feedback
                if(Util.commandRanCorrectly(response)){
                    System.out.println("command executed properly i guess");
                }
            }
        }
    }

    //wait for the setDVCStatus function to perform "DVC status" which is executed async,
    // but DVCFileList needs the information causing a race condition
    private void waitForStatus(){
        while(waitingForStatus){
            try {
                Thread.sleep(0);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
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
                Vector<String> checkedFiles = new Vector<>();
                for(int i=0; i<fileList.getModel().getSize(); i++){
                    if(fileList.getModel().getElementAt(i).isSelected()){
                        checkedFiles.add(fileList.getModel().getElementAt(i).toString());
                    }
                }
                CheckListItem[] files = new CheckListItem[status.length()];

                if(status.length() == 0){ //emtpy file list
                    fileLabel.setText("There are no files tracked yet");
                }
                else {
                    waitingForStatus = true;
                    setDVCStatus();
                    waitForStatus();
                    for(int i=0; i<status.length(); i++){ //
                        JSONObject file = (JSONObject) status.get(i);
                        String fileName = file.getString("path");
                        Color fileColor;

                        if( dvcStatus.containsKey(fileName)){
                            String fileStatus = dvcStatus.get(fileName);
                            fileColor = colorMap.get(fileStatus);
                        }
                        else{
                            fileColor = JBColor.GREEN;
                        }
                        files[i] = new CheckListItem(fileName, fileColor);
                        if(checkedFiles.contains(fileName)){
                            files[i].setSelected(true);
                        }

                    }
                    fileLabel.setText("Your tracked files:");
                    fileList.setListData(files);
                }
            }
        });
        fileList.setCellRenderer(new ColoredListRenderer());
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();
                int index = list.locationToIndex(event.getPoint());// Get index of item
                // clicked
                CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
                item.setSelected(!item.isSelected()); // Toggle selected state
                list.repaint(list.getCellBounds(index, index));// Repaint cell
            }
        });

        if(!Util.commandRanCorrectly(response)){
            fileLabel.setText(response);
        }
    }

    private void setDVCStatus() {
        String dvcStatusCommand = "dvc status --show-json";
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
                waitingForStatus = false;
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
        private boolean isSelected = false;
        private final Color color;

        public CheckListItem(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public Color color() {
            return this.color;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class ColoredListRenderer extends JCheckBox implements ListCellRenderer {
        //protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            //Font theFont;
            Color theForeground;
            //Icon theIcon;
            String theText;

            if (value instanceof CheckListItem) {
                theForeground = ((CheckListItem) value).color();
                //theIcon = (Icon) values[2];
                theText = value.toString();
            } else {
                //theFont = list.getFont();
                theForeground = list.getForeground();
                theText = "";
            }
        /*if (!isSelected) {
            renderer.setForeground(theForeground);
        }*/
        /*if (theIcon != null) {
            renderer.setIcon(theIcon);
        }*/
            setText(theText);
            //renderer.setFont(theFont);
            setEnabled(list.isEnabled());
            setSelected(((CheckListItem) value).isSelected());
            setBackground(list.getBackground());
            setForeground(theForeground);
            //renderer.setText(value.toString());

            return this;
        }
    }
}
