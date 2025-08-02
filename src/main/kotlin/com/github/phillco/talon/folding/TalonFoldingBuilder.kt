package com.github.phillco.talon.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.github.phillco.talon.psi.TalonFile

class TalonFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root !is TalonFile) return emptyArray()
        
        val descriptors = mutableListOf<FoldingDescriptor>()
        val text = document.text
        
        // Find multiline constructs by analyzing the text directly
        var lineStart = 0
        var inMultilineConstruct = false
        var constructStartLine = -1
        var colonLine = -1
        
        for (lineNum in 0 until document.lineCount) {
            val lineEnd = document.getLineEndOffset(lineNum)
            val line = text.substring(lineStart, lineEnd).trim()
            
            when {
                // Check for command/binding start (ends with colon)
                line.endsWith(":") && !line.startsWith("#") -> {
                    colonLine = lineNum
                    constructStartLine = lineNum
                    inMultilineConstruct = true
                }
                
                // Check for separator line
                line.matches(Regex("^-{3,}$")) -> {
                    // End any current construct
                    if (inMultilineConstruct && colonLine >= 0 && lineNum > colonLine + 1) {
                        createFoldingDescriptor(root, document, colonLine, lineNum - 1, descriptors)
                    }
                    inMultilineConstruct = false
                    colonLine = -1
                }
                
                // Empty line or line with only whitespace
                line.isEmpty() -> {
                    // End current construct if we have one
                    if (inMultilineConstruct && colonLine >= 0 && lineNum > colonLine + 1) {
                        createFoldingDescriptor(root, document, colonLine, lineNum - 1, descriptors)
                    }
                    inMultilineConstruct = false
                    colonLine = -1
                }
                
                // Non-empty line
                else -> {
                    // If we're not in a construct and this line doesn't start with whitespace,
                    // it might be a new command
                    if (!inMultilineConstruct && !line.startsWith(" ") && !line.startsWith("\t")) {
                        // Check if it's a potential command start (but wait for colon)
                        // Do nothing for now
                    }
                    // If we are in a construct and hit a non-indented line, end the construct
                    else if (inMultilineConstruct && colonLine >= 0 && 
                             !text[lineStart].isWhitespace() && !line.startsWith("#")) {
                        if (lineNum > colonLine + 1) {
                            createFoldingDescriptor(root, document, colonLine, lineNum - 1, descriptors)
                        }
                        inMultilineConstruct = false
                        colonLine = -1
                    }
                }
            }
            
            lineStart = if (lineNum + 1 < document.lineCount) {
                document.getLineStartOffset(lineNum + 1)
            } else {
                lineEnd
            }
        }
        
        // Handle any remaining construct at end of file
        if (inMultilineConstruct && colonLine >= 0 && document.lineCount > colonLine + 1) {
            createFoldingDescriptor(root, document, colonLine, document.lineCount - 1, descriptors)
        }
        
        return descriptors.toTypedArray()
    }
    
    private fun createFoldingDescriptor(
        root: PsiElement,
        document: Document,
        startLine: Int,
        endLine: Int,
        descriptors: MutableList<FoldingDescriptor>
    ) {
        val startOffset = document.getLineEndOffset(startLine)
        val endOffset = document.getLineEndOffset(endLine)
        
        if (endOffset > startOffset) {
            val range = TextRange(startOffset, endOffset)
            descriptors.add(FoldingDescriptor(root.node, range, FoldingGroup.newGroup("talon")))
        }
    }
    
    override fun getPlaceholderText(node: ASTNode): String = " ..."
    
    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}