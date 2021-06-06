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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

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
                Vector<String> files = new Vector<>();
                if(status.length() == 0){ //emtpy file list
                    fileLabel.setText("There are no files tracked yet");
                }
                else {
                    Iterator itr = status.iterator();
                    while(itr.hasNext()) {
                        JSONObject file = (JSONObject)itr.next();
                        String filename = file.getString("path");
                        if( !Arrays.stream(notListed).anyMatch(filename::equals) &&
                            !(filename.substring(filename.length()-4,filename.length()).equals(".dvc")))
                            {
                                files.add((String) filename);
                            }
                        }
                    }
                fileLabel.setText("Your tracked files:");
                fileList.setListData(files);
                }
        });
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
                System.out.println(commandOutput);
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
