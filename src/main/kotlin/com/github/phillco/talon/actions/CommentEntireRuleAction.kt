package com.github.phillco.talon.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.github.phillco.talon.TalonLanguage

class CommentEntireRuleAction : AnAction() {
    
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = file?.language == TalonLanguage
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        if (file.language != TalonLanguage) return
        
        WriteCommandAction.runWriteCommandAction(project) {
            commentEntireRule(editor, project)
        }
    }
    
    private fun commentEntireRule(editor: Editor, project: Project) {
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val currentLine = document.getLineNumber(caretOffset)
        
        // Find the start of the rule (line ending with :)
        var ruleLine = currentLine
        for (line in currentLine downTo 0) {
            val lineText = getLineText(document, line)
            if (lineText.trim().endsWith(":") && !lineText.trim().startsWith("#")) {
                ruleLine = line
                break
            }
            // Stop if we hit an empty line or separator
            if (lineText.trim().isEmpty() || lineText.trim().matches(Regex("^-{3,}$"))) {
                return // Not in a rule
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
        
        // Check if the rule is already commented
        val firstLineText = getLineText(document, ruleLine)
        val isCommented = firstLineText.trim().startsWith("#")
        
        // Comment or uncomment each line
        for (line in ruleLine..endLine) {
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            val lineText = document.getText(TextRange(lineStart, lineEnd))
            
            if (isCommented) {
                // Uncomment
                val trimmed = lineText.trimStart()
                if (trimmed.startsWith("# ")) {
                    val leadingWhitespace = lineText.length - trimmed.length
                    document.replaceString(lineStart + leadingWhitespace, lineStart + leadingWhitespace + 2, "")
                } else if (trimmed.startsWith("#")) {
                    val leadingWhitespace = lineText.length - trimmed.length
                    document.replaceString(lineStart + leadingWhitespace, lineStart + leadingWhitespace + 1, "")
                }
            } else {
                // Comment
                if (lineText.trim().isNotEmpty()) {
                    val leadingWhitespace = lineText.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
                    document.insertString(lineStart + leadingWhitespace, "# ")
                }
            }
        }
    }
    
    private fun getLineText(document: com.intellij.openapi.editor.Document, line: Int): String {
        if (line < 0 || line >= document.lineCount) return ""
        return document.getText(TextRange(
            document.getLineStartOffset(line),
            document.getLineEndOffset(line)
        ))
    }
}