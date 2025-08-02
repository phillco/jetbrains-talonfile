package com.github.phillco.talon.actions

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.github.phillco.talon.TalonLanguage

/**
 * Smart comment action for Talon files that comments entire rules when on the key line.
 * This action replaces the default CommentByLineComment action for Talon files.
 */
class TalonSmartCommentAction : CommentByLineCommentAction() {
    
    override fun update(e: AnActionEvent) {
        super.update(e)
        
        // Only enable for Talon files
        val file = e.getData(CommonDataKeys.PSI_FILE)
        if (file?.language != TalonLanguage) {
            e.presentation.isEnabledAndVisible = false
        }
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        if (file.language != TalonLanguage) {
            // Fall back to default behavior for non-Talon files
            super.actionPerformed(e)
            return
        }
        
        WriteCommandAction.runWriteCommandAction(project) {
            handleTalonComment(editor, project)
        }
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
    
    private fun toggleRuleComment(startLine: Int, endLine: Int, document: com.intellij.openapi.editor.Document) {
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
    
    private fun toggleLineComment(line: Int, document: com.intellij.openapi.editor.Document) {
        val lineText = getLineText(document, line)
        if (lineText.trim().startsWith("#")) {
            uncommentLine(line, document)
        } else {
            commentLine(line, document)
        }
    }
    
    private fun commentLine(line: Int, document: com.intellij.openapi.editor.Document) {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        
        if (lineText.trim().isNotEmpty() && !lineText.trim().startsWith("#")) {
            val leadingWhitespace = lineText.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
            document.insertString(lineStart + leadingWhitespace, "# ")
        }
    }
    
    private fun uncommentLine(line: Int, document: com.intellij.openapi.editor.Document) {
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
    
    private fun findRuleRange(startLine: Int, document: com.intellij.openapi.editor.Document): Pair<Int, Int> {
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
    
    private fun getLineText(document: com.intellij.openapi.editor.Document, line: Int): String {
        if (line < 0 || line >= document.lineCount) return ""
        return document.getText(TextRange(
            document.getLineStartOffset(line),
            document.getLineEndOffset(line)
        ))
    }
}