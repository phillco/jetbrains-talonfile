package com.github.phillco.talon.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.github.phillco.talon.TalonLanguage
import com.github.phillco.talon.psi.impl.TalonPsiElementImpl

object TalonElementTypes {
    @JvmField val COMMAND = IElementType("COMMAND", TalonLanguage)
    @JvmField val MATCH = IElementType("MATCH", TalonLanguage)
    @JvmField val STATEMENT = IElementType("STATEMENT", TalonLanguage)
    @JvmField val EXPRESSION = IElementType("EXPRESSION", TalonLanguage)
    @JvmField val RULE = IElementType("RULE", TalonLanguage)
    @JvmField val BINDING = IElementType("BINDING", TalonLanguage)
    
    object Factory {
        fun createElement(node: ASTNode): PsiElement {
            return TalonPsiElementImpl(node)
        }
    }
}