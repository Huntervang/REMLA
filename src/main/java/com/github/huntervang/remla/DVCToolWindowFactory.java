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

public class DVCToolWindowFactory implements ToolWindowFactory {

    private boolean isDvcInstalled;
    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        checkDvcInstalled();
        DVCAddRemote dvcAddRemote = new DVCAddRemote(toolWindow);
        NoDVCProjectWindow noDVCProjectWindow = new NoDVCProjectWindow();
        NoDVCInstalledWindow noDVCInstalledWindow = new NoDVCInstalledWindow();


        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content;
        System.out.println(project.getBasePath());
        System.out.println(Util.isDvcInstalled(project.getBasePath()));
        if (!isDvcInstalled) {
            content = contentFactory.createContent(noDVCInstalledWindow.getContent(), "", false);
        } else {
            if (!Util.isDvcInstalled(project.getBasePath())) {
                content = contentFactory.createContent(noDVCProjectWindow.getContent(), "", false);
            } else {
                content = contentFactory.createContent(dvcAddRemote.getContent(), "", false);
            }
        }

        toolWindow.getContentManager().addContent(content);
    }

    private void checkDvcInstalled() {
        String message = Util.runConsoleCommand("which dvc", Util.getProject().getBasePath(), new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                isDvcInstalled = true;
            }
        });
    }

}
