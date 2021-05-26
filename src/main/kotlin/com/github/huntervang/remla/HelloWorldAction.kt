package com.github.huntervang.remla

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class HelloWorldAction: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog(e.project, "Hello world!", "Hello Window", Messages.getInformationIcon())
    }
}