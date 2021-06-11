package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DVCToolWindowFactory implements ToolWindowFactory {

    public static HashMap<String, Content> contentHashMap = new HashMap<>();

    private boolean isDvcInstalled;
    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        checkDvcInstalled();
        DVCAddRemote dvcAddRemoteWindow = new DVCAddRemote(toolWindow);
        NoDVCProjectWindow noDVCProjectWindow = new NoDVCProjectWindow(toolWindow);
        NoDVCInstalledWindow noDVCInstalledWindow = new NoDVCInstalledWindow();


        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content noDVCInstalledContent = contentFactory.createContent(noDVCInstalledWindow.getContent(), "", false);
        Content noDVCProjectContent = contentFactory.createContent(noDVCProjectWindow.getContent(), "", false);
        Content dvcAddRemoteContent = contentFactory.createContent(dvcAddRemoteWindow.getContent(), "", false);

        contentHashMap.put("no_installation", noDVCInstalledContent);
        contentHashMap.put("no_project", noDVCProjectContent);
        contentHashMap.put("dvc_add", dvcAddRemoteContent);
        toolWindow.getContentManager().addContent(noDVCInstalledContent);
        toolWindow.getContentManager().addContent(noDVCProjectContent);
        toolWindow.getContentManager().addContent(dvcAddRemoteContent);

        if (!isDvcInstalled) {
            toolWindow.getContentManager().setSelectedContent(noDVCInstalledContent, true);
        } else {
            if (!Util.isDvcInProject(project.getBasePath())) {
                toolWindow.getContentManager().setSelectedContent(noDVCProjectContent, true);
            } else {
                toolWindow.getContentManager().setSelectedContent(dvcAddRemoteContent, true);
            }
        }
    }

    private void checkDvcInstalled() {
        Util.runConsoleCommand("which dvc", Util.getProject().getBasePath(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                isDvcInstalled = true;
            }
        });
    }

}
