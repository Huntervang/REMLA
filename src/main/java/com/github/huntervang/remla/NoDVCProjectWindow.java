package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.github.huntervang.remla.DVCToolWindowFactory.dvcAddRemoteContent;

public class NoDVCProjectWindow {
	private JPanel noDVCProjectWindow;
	private JButton initializeDVCProjectButton;

	public NoDVCProjectWindow(ToolWindow toolWindow) {
		initializeDVCProjectButton.addActionListener(e -> initializeProject(toolWindow));
	}

	private void initializeProject(ToolWindow toolWindow) {
		Util.runConsoleCommand("dvc init", Util.getProject().getBasePath(), new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				super.processTerminated(event);
				ApplicationManager.getApplication().invokeLater(() -> toolWindow.getContentManager().setSelectedContent(dvcAddRemoteContent));
			}
		});
	}

	public JPanel getContent() {
		return noDVCProjectWindow;
	}

}
