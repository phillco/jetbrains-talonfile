package com.github.phillco.talon.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.github.phillco.talon.lexer.TalonTokenTypes
import com.github.phillco.talon.parser.TalonElementTypes
import com.github.phillco.talon.psi.TalonFile

class TalonFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root !is TalonFile) return emptyArray()
        
        val descriptors = mutableListOf<FoldingDescriptor>()
        
        // Find all commands and bindings that span multiple lines
        val visitor = object : com.intellij.psi.PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                
                val node = element.node
                if (node != null && isFoldableElement(node)) {
                    val range = getRangeToFold(node, document)
                    if (range != null && range.length > 0) {
                        descriptors.add(FoldingDescriptor(node, range, FoldingGroup.newGroup("talon")))
                    }
                }
            }
        }
        
        root.accept(visitor)
        
        return descriptors.toTypedArray()
    }
    
    private fun isFoldableElement(node: ASTNode): Boolean {
        return when (node.elementType) {
            TalonElementTypes.COMMAND,
            TalonElementTypes.BINDING,
            TalonElementTypes.STATEMENT -> true
            else -> false
        }
    }
    
    private fun getRangeToFold(node: ASTNode, document: Document): TextRange? {
        val startOffset = node.startOffset
        val endOffset = node.startOffset + node.textLength
        
        // Check if this spans multiple lines
        val startLine = document.getLineNumber(startOffset)
        val endLine = document.getLineNumber(endOffset)
        
        if (endLine > startLine) {
            // Fold from the end of the first line to the end of the block
            val firstLineEnd = document.getLineEndOffset(startLine)
            return TextRange(firstLineEnd, endOffset)
        }
        
        return null
    }
    
    override fun getPlaceholderText(node: ASTNode): String {
        return when (node.elementType) {
            TalonElementTypes.COMMAND -> " ..."
            TalonElementTypes.BINDING -> " { ... }"
            TalonElementTypes.STATEMENT -> " ..."
            else -> "..."
        }
    }
    
    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}