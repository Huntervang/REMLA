package com.github.huntervang.remla;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class NoDVCProjectWindow {
	private JPanel noDVCProjectWindow;
	private JButton initializeDVCProjectButton;

	public NoDVCProjectWindow() {
		initializeDVCProjectButton.addActionListener(e -> initializeProject());
	}

	private void initializeProject() {
		Util.runConsoleCommand("dvc init", Util.getProject().getBasePath(), new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				super.processTerminated(event);
			}
		});
	}

	public JPanel getContent() {
		return noDVCProjectWindow;
	}


}
