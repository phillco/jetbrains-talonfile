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
            
            // Keywords
            TalonTokenTypes.AND, TalonTokenTypes.NOT, TalonTokenTypes.OR -> arrayOf(KEYWORD)
            
            // Context keywords (more prominent)
            TalonTokenTypes.SETTINGS, TalonTokenTypes.TAG, TalonTokenTypes.APP,
            TalonTokenTypes.KEY, TalonTokenTypes.FACE, TalonTokenTypes.DECK,
            TalonTokenTypes.GAMEPAD, TalonTokenTypes.NOISE, TalonTokenTypes.PARROT,
            TalonTokenTypes.MODE -> arrayOf(CONTEXT_KEYWORD)
            
            // Special identifiers
            TalonTokenTypes.USER, TalonTokenTypes.SELF -> arrayOf(BUILTIN_NAME)
            
            // Literals
            TalonTokenTypes.STRING -> arrayOf(STRING)
            TalonTokenTypes.NUMBER -> arrayOf(NUMBER)
            TalonTokenTypes.REGEX -> arrayOf(REGEX)
            TalonTokenTypes.PATH -> arrayOf(PATH)
            TalonTokenTypes.WORD -> arrayOf(COMMAND_WORD)
            
            // Function and action names
            TalonTokenTypes.FUNCTION_NAME -> arrayOf(FUNCTION_CALL)
            TalonTokenTypes.ACTION_NAME -> arrayOf(ACTION_CALL)
            TalonTokenTypes.VARIABLE_REFERENCE -> arrayOf(VARIABLE_REF)
            
            // Operators
            TalonTokenTypes.COLON, TalonTokenTypes.EQ -> arrayOf(OPERATION_SIGN)
            TalonTokenTypes.PIPE -> arrayOf(PIPE_OPERATOR)
            TalonTokenTypes.CARET, TalonTokenTypes.DOLLAR -> arrayOf(REGEX_DELIMITER)
            
            // Structural
            TalonTokenTypes.LPAREN, TalonTokenTypes.RPAREN,
            TalonTokenTypes.LBRACKET, TalonTokenTypes.RBRACKET,
            TalonTokenTypes.LBRACE, TalonTokenTypes.RBRACE -> arrayOf(BRACES)
            
            TalonTokenTypes.COMMA -> arrayOf(COMMA)
            TalonTokenTypes.DOT -> arrayOf(DOT)
            TalonTokenTypes.DASH -> arrayOf(SEPARATOR)
            
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
        
        private val CONTEXT_KEYWORD = TextAttributesKey.createTextAttributesKey(
            "TALON_CONTEXT_KEYWORD",
            DefaultLanguageHighlighterColors.METADATA
        )
        
        private val BUILTIN_NAME = TextAttributesKey.createTextAttributesKey(
            "TALON_BUILTIN_NAME",
            DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL
        )
        
        private val STRING = TextAttributesKey.createTextAttributesKey(
            "TALON_STRING",
            DefaultLanguageHighlighterColors.STRING
        )
        
        private val NUMBER = TextAttributesKey.createTextAttributesKey(
            "TALON_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )
        
        private val REGEX = TextAttributesKey.createTextAttributesKey(
            "TALON_REGEX",
            DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
        )
        
        private val PATH = TextAttributesKey.createTextAttributesKey(
            "TALON_PATH",
            DefaultLanguageHighlighterColors.STRING
        )
        
        private val COMMAND_WORD = TextAttributesKey.createTextAttributesKey(
            "TALON_COMMAND_WORD",
            DefaultLanguageHighlighterColors.LOCAL_VARIABLE
        )
        
        private val FUNCTION_CALL = TextAttributesKey.createTextAttributesKey(
            "TALON_FUNCTION_CALL",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )
        
        private val ACTION_CALL = TextAttributesKey.createTextAttributesKey(
            "TALON_ACTION_CALL",
            DefaultLanguageHighlighterColors.STATIC_METHOD
        )
        
        private val VARIABLE_REF = TextAttributesKey.createTextAttributesKey(
            "TALON_VARIABLE_REF",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD
        )
        
        private val OPERATION_SIGN = TextAttributesKey.createTextAttributesKey(
            "TALON_OPERATION_SIGN",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        
        private val PIPE_OPERATOR = TextAttributesKey.createTextAttributesKey(
            "TALON_PIPE_OPERATOR",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        
        private val REGEX_DELIMITER = TextAttributesKey.createTextAttributesKey(
            "TALON_REGEX_DELIMITER",
            DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
        )
        
        private val SEPARATOR = TextAttributesKey.createTextAttributesKey(
            "TALON_SEPARATOR",
            DefaultLanguageHighlighterColors.COMMA
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