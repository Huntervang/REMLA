package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class NoDVCProjectWindow {
	private JPanel noDVCProjectWindow;
	private JButton initializeDVCProjectButton;

	public NoDVCProjectWindow(Project project, DVCToolWindowFactory dvcToolWindowFactory) {
		initializeDVCProjectButton.addActionListener(e -> initializeProject(project, dvcToolWindowFactory));
	}

	private void initializeProject(Project project, DVCToolWindowFactory dvcToolWindowFactory) {
		Util.runConsoleCommand("dvc init", Util.getProject().getBasePath(), new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				if (!Util.isGitInProject(project.getBasePath())) {
					ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(project,
						"This project is not a git project. If you want to setup a new project, run `git init` in your commandline",
						"Not a Git Project"
					));
					return;
				}
				super.processTerminated(event);
				ApplicationManager.getApplication().invokeLater(dvcToolWindowFactory::setDvcInProjectView);
			}
		});

	}

	public JPanel getContent() {
		return noDVCProjectWindow;
	}

}
