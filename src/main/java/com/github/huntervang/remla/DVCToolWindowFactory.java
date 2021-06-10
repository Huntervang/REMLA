package com.github.huntervang.remla;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class DVCToolWindowFactory implements ToolWindowFactory {
    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DVCAddRemote dvcAddRemote = new DVCAddRemote(toolWindow);
        NoDVCInitWindow noDVCInitWindow = new NoDVCInitWindow(toolWindow);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content;
        System.out.println(project.getBasePath());
        System.out.println(Util.isDvcInstalled(project.getBasePath()));
        if (!Util.isDvcInstalled(project.getBasePath())) {
            content = contentFactory.createContent(noDVCInitWindow.getContent(), "", false);
        } else {
            content = contentFactory.createContent(dvcAddRemote.getContent(), "", false);
        }

        toolWindow.getContentManager().addContent(content);
    }
}
