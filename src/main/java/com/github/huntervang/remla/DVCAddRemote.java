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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.awt.Color;

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

    public DVCAddRemote(ToolWindow toolWindow) {
        project = ProjectManager.getInstance().getOpenProjects()[0];
        addStorageButton.addActionListener(e -> dvcAddRemote());
        setDVCFiles();
        setDVCFileList();
        //fileLabel.setIcon(Icon("DVC.png"));
    }

    private boolean commandRanCorrectly(String message){

        if(message.equals(messageIfRanCorrectly))
            return true;
        else
            return false;
    }

    private void setDVCFileList() {
        String dvcList = "dvc list . -R --show-json"; //TODO handle folder path
        String response = runConsoleCommand(dvcList, new ProcessAdapter() {
            JSONArray status;
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    System.out.println(commandOutput);
                    status = new JSONArray(commandOutput);
                }
                catch(org.json.JSONException e){
                    status = new JSONArray();
                }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);

                Object files[][] = new Object[status.length()][2];

                if(status.length() == 0){ //emtpy file list
                    fileLabel.setText("There are no files tracked yet");
                }
                else {
                    for(int i=0; i<status.length(); i++){ //
                        JSONObject file = (JSONObject) status.get(i);
                        String filename = file.getString("path");

                        //TODO Parse DVC Status: get status per file

                        if( !Arrays.stream(notListed).anyMatch(filename::equals) &&
                            !(filename.substring(filename.length()-4,filename.length()).equals(".dvc")))
                        {
                            //TODO : adapt color on status info
                            Object fileColor = Color.green;
                            files[i][0] = fileColor;
                            files[i][1] = (String) filename;
                        }
                    }
                    fileLabel.setText("Your tracked files:");
                    fileList.setListData(files);
                }
            }
        });
        ListCellRenderer renderer = new ColoredListRenderer();
        fileList.setCellRenderer(renderer);

        if(!commandRanCorrectly(response)){
            fileLabel.setText((String) response);
        }
    }

    //TODO parse DVC status and handle differences
    private void setDVCFiles() {
        String dvcStatus = "dvc status --show-json";
        String response = runConsoleCommand(dvcStatus, new ProcessAdapter() {
            JSONObject status;
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                try {
                    String commandOutput = event.getText();
                    System.out.println(commandOutput);
                    status = new JSONObject(commandOutput);
                }
                catch(org.json.JSONException e){
                    status = new JSONObject();
                }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                if(status.keySet().size() == 0){ //emtpy file list
                    fileLabel.setText("There are no files tracked yet");
                }
                else {
                    for (Object filename : status.keySet()) {
                        fileLabel.setText((String) filename);
                        System.out.println((String) filename);
                    }
                }
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
            System.out.println("here");
            return errorMessage;
        }
    }
}

class ColoredListRenderer implements ListCellRenderer {
    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        //Font theFont = null;
        Color theForeground = null;
        //Icon theIcon = null;
        String theText = null;

        JLabel renderer = (JLabel) defaultRenderer
                .getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);

        if (value instanceof Object[]) {
            Object values[] = (Object[]) value;
            //theFont = (Font) values[0];
            theForeground = (Color) values[0];
            //theIcon = (Icon) values[2];
            theText = (String) values[1];
        } else {
            //theFont = list.getFont();
            theForeground = list.getForeground();
            theText = "";
        }
        if (!isSelected) {
            renderer.setForeground(theForeground);
        }
        /*if (theIcon != null) {
            renderer.setIcon(theIcon);
        }*/
        renderer.setText(theText);
        //renderer.setFont(theFont);
        return renderer;
    }
}
