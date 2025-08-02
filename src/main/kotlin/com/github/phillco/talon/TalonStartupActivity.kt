package com.github.phillco.talon

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.TextRange

/**
 * Startup activity that registers custom handlers for Talon files.
 */
class TalonStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        // Register our action to override the default comment action
        val actionManager = ActionManager.getInstance()
        
        // Create a wrapper action that checks for Talon files first
        val originalAction = actionManager.getAction("CommentByLineComment")
        if (originalAction != null) {
            val wrapperAction = TalonAwareCommentAction(originalAction)
            actionManager.replaceAction("CommentByLineComment", wrapperAction)
        }
    }
}

/**
 * Wrapper action that delegates to Talon handler for Talon files, 
 * or to the original action otherwise.
 */
class TalonAwareCommentAction(private val originalAction: AnAction) : AnAction() {
    
    init {
        // Copy presentation from original action
        templatePresentation.copyFrom(originalAction.templatePresentation)
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        
        if (file?.language == TalonLanguage) {
            // Use our smart handler for Talon files
            val project = e.project ?: return
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return
            
            WriteCommandAction.runWriteCommandAction(project) {
                handleTalonComment(editor, project)
            }
        } else {
            // Delegate to original action for other files
            originalAction.actionPerformed(e)
        }
    }
    
    override fun update(e: AnActionEvent) {
        originalAction.update(e)
    }
    
    private fun handleTalonComment(editor: Editor, project: Project) {
        val document = editor.document
        val caretModel = editor.caretModel
        
        if (caretModel.caretCount > 1) {
            // Multiple carets - comment each line individually
            caretModel.allCarets.forEach { caret ->
                val line = document.getLineNumber(caret.offset)
                toggleLineComment(line, document)
            }
        } else {
            val selectionModel = editor.selectionModel
            
            if (selectionModel.hasSelection()) {
                // Selection mode - comment selected lines
                val startLine = document.getLineNumber(selectionModel.selectionStart)
                val endLine = document.getLineNumber(selectionModel.selectionEnd)
                
                for (line in startLine..endLine) {
                    toggleLineComment(line, document)
                }
            } else {
                // No selection - check if we're on a key line
                val currentLine = document.getLineNumber(caretModel.offset)
                val lineText = getLineText(document, currentLine)
                
                if (shouldCommentEntireRule(lineText)) {
                    // Comment entire rule
                    val ruleRange = findRuleRange(currentLine, document)
                    toggleRuleComment(ruleRange.first, ruleRange.second, document)
                } else {
                    // Comment single line
                    toggleLineComment(currentLine, document)
                }
            }
        }
    }
    
    private fun shouldCommentEntireRule(lineText: String): Boolean {
        // Check if current line is a key line (ends with colon)
        return lineText.trim().endsWith(":") && !lineText.trim().startsWith("#")
    }
    
    private fun toggleRuleComment(startLine: Int, endLine: Int, document: Document) {
        // Check if the rule is already commented
        val firstLineText = getLineText(document, startLine)
        val isCommented = firstLineText.trim().startsWith("#")
        
        // Toggle comment for all lines in the rule
        for (line in startLine..endLine) {
            if (isCommented) {
                uncommentLine(line, document)
            } else {
                commentLine(line, document)
            }
        }
    }
    
    private fun toggleLineComment(line: Int, document: Document) {
        val lineText = getLineText(document, line)
        if (lineText.trim().startsWith("#")) {
            uncommentLine(line, document)
        } else {
            commentLine(line, document)
        }
    }
    
    private fun commentLine(line: Int, document: Document) {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        
        if (lineText.trim().isNotEmpty() && !lineText.trim().startsWith("#")) {
            val leadingWhitespace = lineText.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
            document.insertString(lineStart + leadingWhitespace, "# ")
        }
    }
    
    private fun uncommentLine(line: Int, document: Document) {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        val trimmed = lineText.trimStart()
        
        if (trimmed.startsWith("# ")) {
            val leadingWhitespace = lineText.length - trimmed.length
            document.deleteString(lineStart + leadingWhitespace, lineStart + leadingWhitespace + 2)
        } else if (trimmed.startsWith("#")) {
            val leadingWhitespace = lineText.length - trimmed.length
            document.deleteString(lineStart + leadingWhitespace, lineStart + leadingWhitespace + 1)
        }
    }
    
    private fun findRuleRange(startLine: Int, document: Document): Pair<Int, Int> {
        // Find the start of the rule (line ending with :)
        var ruleLine = startLine
        for (line in startLine downTo 0) {
            val lineText = getLineText(document, line)
            if (lineText.trim().endsWith(":") && !lineText.trim().startsWith("#")) {
                ruleLine = line
                break
            }
            // Stop if we hit an empty line or separator
            if (lineText.trim().isEmpty() || lineText.trim().matches(Regex("^-{3,}$"))) {
                break
            }
        }
        
        // Find the end of the rule
        var endLine = ruleLine
        for (line in (ruleLine + 1) until document.lineCount) {
            val lineText = getLineText(document, line)
            
            when {
                // Empty line ends the rule
                lineText.trim().isEmpty() -> break
                
                // Separator line ends the rule
                lineText.trim().matches(Regex("^-{3,}$")) -> break
                
                // Non-indented line (new rule) ends current rule
                lineText.isNotEmpty() && !lineText[0].isWhitespace() && !lineText.startsWith("#") -> break
                
                // Otherwise, this line is part of the rule
                else -> endLine = line
            }
        }
        
        return Pair(ruleLine, endLine)
    }
    
    private fun getLineText(document: Document, line: Int): String {
        if (line < 0 || line >= document.lineCount) return ""
        return document.getText(TextRange(
            document.getLineStartOffset(line),
            document.getLineEndOffset(line)
        ))
    }
}