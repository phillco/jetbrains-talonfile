package com.github.phillco.talon.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.github.phillco.talon.lexer.TalonLexer
import com.github.phillco.talon.lexer.TalonTokenTypes

class TalonSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = TalonLexer()
    
    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            TalonTokenTypes.COMMENT -> arrayOf(COMMENT)
            
            TalonTokenTypes.AND, TalonTokenTypes.NOT, TalonTokenTypes.OR,
            TalonTokenTypes.SETTINGS, TalonTokenTypes.TAG, TalonTokenTypes.APP,
            TalonTokenTypes.KEY, TalonTokenTypes.FACE, TalonTokenTypes.DECK,
            TalonTokenTypes.GAMEPAD, TalonTokenTypes.NOISE, TalonTokenTypes.PARROT -> arrayOf(KEYWORD)
            
            TalonTokenTypes.STRING -> arrayOf(STRING)
            TalonTokenTypes.NUMBER -> arrayOf(NUMBER)
            
            TalonTokenTypes.COLON, TalonTokenTypes.EQ -> arrayOf(OPERATION_SIGN)
            
            TalonTokenTypes.LPAREN, TalonTokenTypes.RPAREN,
            TalonTokenTypes.LBRACKET, TalonTokenTypes.RBRACKET,
            TalonTokenTypes.LBRACE, TalonTokenTypes.RBRACE -> arrayOf(BRACES)
            
            TalonTokenTypes.COMMA -> arrayOf(COMMA)
            TalonTokenTypes.DOT -> arrayOf(DOT)
            
            TokenType.BAD_CHARACTER -> arrayOf(BAD_CHARACTER)
            
            else -> EMPTY
        }
    }
    
    companion object {
        private val COMMENT = TextAttributesKey.createTextAttributesKey(
            "TALON_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        
        private val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "TALON_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        
        private val STRING = TextAttributesKey.createTextAttributesKey(
            "TALON_STRING",
            DefaultLanguageHighlighterColors.STRING
        )
        
        private val NUMBER = TextAttributesKey.createTextAttributesKey(
            "TALON_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )
        
        private val OPERATION_SIGN = TextAttributesKey.createTextAttributesKey(
            "TALON_OPERATION_SIGN",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        
        private val BRACES = TextAttributesKey.createTextAttributesKey(
            "TALON_BRACES",
            DefaultLanguageHighlighterColors.BRACES
        )
        
        private val COMMA = TextAttributesKey.createTextAttributesKey(
            "TALON_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )
        
        private val DOT = TextAttributesKey.createTextAttributesKey(
            "TALON_DOT",
            DefaultLanguageHighlighterColors.DOT
        )
        
        private val BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
            "TALON_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
        )
        
        private val EMPTY = emptyArray<TextAttributesKey>()
    }
}