package com.github.phillco.talon

import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.github.phillco.talon.actions.TalonCommentHandler

/**
 * Startup activity that registers custom handlers for Talon files.
 */
class TalonStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        // Register our custom comment handler
        val actionManager = EditorActionManager.getInstance()
        val handler = TalonCommentHandler()
        actionManager.setActionHandler("CommentByLineComment", handler)
    }
}