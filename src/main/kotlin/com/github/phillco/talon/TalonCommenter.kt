package com.github.phillco.talon

import com.intellij.codeInsight.generation.IndentedCommenter
import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import com.intellij.psi.tree.IElementType

class TalonCommenter : CodeDocumentationAwareCommenter, IndentedCommenter {
    override fun getLineCommentPrefix(): String = "#"
    
    override fun getBlockCommentPrefix(): String? = null
    
    override fun getBlockCommentSuffix(): String? = null
    
    override fun getCommentedBlockCommentPrefix(): String? = null
    
    override fun getCommentedBlockCommentSuffix(): String? = null
    
    // Documentation comment support (for future use)
    override fun getDocumentationCommentPrefix(): String? = null
    
    override fun getDocumentationCommentLinePrefix(): String? = null
    
    override fun getDocumentationCommentSuffix(): String? = null
    
    override fun getLineCommentTokenType(): IElementType? = null
    
    override fun getBlockCommentTokenType(): IElementType? = null
    
    override fun getDocumentationCommentTokenType(): IElementType? = null
    
    override fun isDocumentationComment(element: PsiComment?): Boolean = false
    
    // IndentedCommenter - this helps with commenting indented blocks
    override fun forceIndentedLineComment(): Boolean = true
}