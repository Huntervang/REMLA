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
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        /*
        DVCToolWindow dvcToolWindow = new DVCToolWindow(project, toolWindow);
        Content content = contentFactory.createContent(dvcToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);

        DVCAddRemote dvcAddRemote = new DVCAddRemote(toolWindow);
        content = contentFactory.createContent(dvcAddRemote.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
         */

        MakeDVCList dvcList = new MakeDVCList(project);
        Content content = contentFactory.createContent(dvcList.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
