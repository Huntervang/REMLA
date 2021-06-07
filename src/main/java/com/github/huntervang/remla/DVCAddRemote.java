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
    private JPanel filePanel;
    private JLabel fileLabel;
    private JScrollPane fileListScroller;
    private JList fileList;
    private JScrollPane changeListScroller;
    private JList changeList;
    private JFileChooser fileChooser;
    private final Project project;

    private final String messageIfRanCorrectly = "";
    private final String[] notListed = {".gitignore", ".dvcignore"};
    private HashMap<String, Color> colorMap = new HashMap<String, Color>() {{   put("new" , Color.green);
                                                                                put("modified" , Color.blue);
                                                                                put("deleted" , Color.gray);
                                                                                put("not in cache" , Color.red); }};

    private boolean waitingForStatus = true;
    private HashMap<String, String> dvcStatus = new HashMap<>();

    public DVCAddRemote(ToolWindow toolWindow) {
        project = ProjectManager.getInstance().getOpenProjects()[0];
        addStorageButton.addActionListener(e -> dvcAddRemote());
        setDVCFileList(); //TODO set filelist is called in first render, should be applied on some trigger, but don't know which trigger
    }

    private boolean commandRanCorrectly(String message){
        return message.equals(messageIfRanCorrectly);
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
        String response = runConsoleCommand(dvcListCommand, new ProcessAdapter() {
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
                CheckListItem[] files = new CheckListItem[status.length()];

                if(status.length() == 0){ //emtpy file list
                    fileLabel.setText("There are no files tracked yet");
                }
                else {
                    for(int i=0; i<status.length(); i++){ //
                        JSONObject file = (JSONObject) status.get(i);
                        String fileName = file.getString("path");

                        waitingForStatus = true;
                        setDVCStatus();
                        waitForStatus();

                        Color fileColor;

                        if( dvcStatus.keySet().contains(fileName)){
                            String fileStatus = dvcStatus.get(fileName);

                            fileColor = colorMap.get(fileStatus);
                        }
                        else{
                            fileColor = Color.green;
                        }
                        files[i] = new CheckListItem(fileName, fileColor);
                    }
                    fileLabel.setText("Your tracked files:");
                    fileList.setListData(files);
                }
            }
        });
        ListCellRenderer renderer = new ColoredListRenderer();
        fileList.setCellRenderer(renderer);
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

        if(!commandRanCorrectly(response)){
            fileLabel.setText((String) response);
        }
    }

    //TODO parse DVC status and handle differences
    private void setDVCStatus() {
        String dvcStatusCommand = "dvc status --show-json";
        String response = runConsoleCommand(dvcStatusCommand, new ProcessAdapter() {
            JSONObject status;
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    System.out.println(commandOutput);
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
        if(!commandRanCorrectly(response)){
            fileLabel.setText((String) response);
        }

    }

    public JPanel getContent() {
        return dvcAddRemoteContent;
    }

    public void dvcAddRemote() {
        String response = runConsoleCommand("dvc remote add myremote " + remoteInput.getText(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                String commandOutput = event.getText();
            }
        });
    }

    private String runConsoleCommand(String command, ProcessAdapter processAdapter) {
        try {
            ArrayList<String> cmds = new ArrayList<>(Arrays.asList(command.split(" ")));

            GeneralCommandLine generalCommandLine = new GeneralCommandLine(cmds);
            generalCommandLine.setCharset(StandardCharsets.UTF_8);
            generalCommandLine.setWorkDirectory(project.getBasePath());

            ProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
            processHandler.startNotify();
            processHandler.addProcessListener(processAdapter);
            return messageIfRanCorrectly;

        } catch (ExecutionException executionException) {
            String errorMessage = executionException.getMessage();
            System.out.println(errorMessage);
            return errorMessage;
        }
    }
}

class CheckListItem {
    private String label;
    private boolean isSelected = false;
    private Color color;

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

class ColoredListRenderer extends JCheckBox implements ListCellRenderer {
    //protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        //Font theFont = null;
        Color theForeground = null;
        //Icon theIcon = null;
        String theText = null;

        /*JCheckBox renderer = (JCheckBox) defaultRenderer
                .getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
        */
        if (value instanceof Object[]) {
            Object values[] = (Object[]) value;
            //theFont = (Font) values[0];
            theForeground = (Color) values[0];
            //theIcon = (Icon) values[2];
            theText = (String) values[1];
        } if (value instanceof CheckListItem) {
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
