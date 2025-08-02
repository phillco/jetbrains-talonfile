package com.github.phillco.talon

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.github.phillco.talon.lexer.TalonTokenTypes

class TalonBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS
    
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
    
    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
    
    companion object {
        private val PAIRS = arrayOf(
            BracePair(TalonTokenTypes.LPAREN, TalonTokenTypes.RPAREN, false),
            BracePair(TalonTokenTypes.LBRACKET, TalonTokenTypes.RBRACKET, false),
            BracePair(TalonTokenTypes.LBRACE, TalonTokenTypes.RBRACE, true)
        )
    }
}