package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class DVCToolWindowFactory implements ToolWindowFactory {
    private ContentManager contentManager;
    private Content dvcToolWindowContent;
    private Content noDVCProjectContent;
    private Content noDVCInstalledContent;
    private Content dvcDagContent;
    private boolean isDvcInProject;

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        checkDvcInstalled(project);
        DVCToolWindow dvcToolWindow = new DVCToolWindow(project, toolWindow);
        NoDVCProjectWindow noDVCProjectWindow = new NoDVCProjectWindow(project,this);
        NoDVCInstalledWindow noDVCInstalledWindow = new NoDVCInstalledWindow();
        DVCDag dvcDag = new DVCDag(project);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        noDVCInstalledContent = contentFactory.createContent(noDVCInstalledWindow.getContent(), "", false);
        noDVCProjectContent = contentFactory.createContent(noDVCProjectWindow.getContent(), "", false);
        dvcToolWindowContent = contentFactory.createContent(dvcToolWindow.getContent(), "DVC", false);
        dvcDagContent = contentFactory.createContent(dvcDag.getContent(), "DVC Dag", false);

        contentManager = toolWindow.getContentManager();
        contentManager.addContent(noDVCInstalledContent);
        contentManager.setSelectedContent(noDVCInstalledContent, true);

        isDvcInProject = Util.isDvcInProject(project.getBasePath());
    }

    private void setDvcInstalledView() {
        contentManager.removeContent(noDVCInstalledContent, true);

        if (!isDvcInProject) {
            contentManager.addContent(noDVCProjectContent);
            contentManager.setSelectedContent(noDVCProjectContent, true);
        } else {
            contentManager.addContent(dvcToolWindowContent);
            contentManager.setSelectedContent(dvcToolWindowContent, true);
            contentManager.addContent(dvcDagContent);
        }
    }

    public void setDvcInProjectView() {
        contentManager.removeContent(noDVCProjectContent, true);

        contentManager.addContent(dvcToolWindowContent);
        contentManager.setSelectedContent(dvcToolWindowContent, true);
    }

    private void checkDvcInstalled(Project project) {
        if (Util.isWindows()) {
            Util.runConsoleCommand("dvc version", project.getBasePath(), new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    super.onTextAvailable(event, outputType);
                    if (event.getText().contains("DVC version")) {
                        ApplicationManager.getApplication().invokeLater(() -> setDvcInstalledView());
                    }
                }
            });
        } else { //Unix
            Util.runConsoleCommand("which dvc", project.getBasePath(), new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    super.onTextAvailable(event, outputType);
                    ApplicationManager.getApplication().invokeLater(() -> setDvcInstalledView());
                }
            });
        }
    }
}
