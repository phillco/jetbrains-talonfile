package com.github.phillco.talon.lexer

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.github.phillco.talon.TalonLanguage

object TalonTokenTypes {
    @JvmField val COMMENT = IElementType("COMMENT", TalonLanguage)
    @JvmField val COLON = IElementType("COLON", TalonLanguage)
    @JvmField val LPAREN = IElementType("LPAREN", TalonLanguage)
    @JvmField val RPAREN = IElementType("RPAREN", TalonLanguage)
    @JvmField val LBRACKET = IElementType("LBRACKET", TalonLanguage)
    @JvmField val RBRACKET = IElementType("RBRACKET", TalonLanguage)
    @JvmField val LBRACE = IElementType("LBRACE", TalonLanguage)
    @JvmField val RBRACE = IElementType("RBRACE", TalonLanguage)
    @JvmField val COMMA = IElementType("COMMA", TalonLanguage)
    @JvmField val DOT = IElementType("DOT", TalonLanguage)
    @JvmField val PIPE = IElementType("PIPE", TalonLanguage)
    @JvmField val DASH = IElementType("DASH", TalonLanguage)
    @JvmField val EQ = IElementType("EQ", TalonLanguage)
    @JvmField val PLUS = IElementType("PLUS", TalonLanguage)
    @JvmField val STAR = IElementType("STAR", TalonLanguage)
    @JvmField val DOLLAR = IElementType("DOLLAR", TalonLanguage)
    @JvmField val LESS = IElementType("LESS", TalonLanguage)
    @JvmField val GREATER = IElementType("GREATER", TalonLanguage)
    
    // Keywords
    @JvmField val AND = IElementType("AND", TalonLanguage)
    @JvmField val NOT = IElementType("NOT", TalonLanguage)
    @JvmField val OR = IElementType("OR", TalonLanguage)
    @JvmField val SETTINGS = IElementType("SETTINGS", TalonLanguage)
    @JvmField val TAG = IElementType("TAG", TalonLanguage)
    @JvmField val APP = IElementType("APP", TalonLanguage)
    @JvmField val KEY = IElementType("KEY", TalonLanguage)
    @JvmField val FACE = IElementType("FACE", TalonLanguage)
    @JvmField val DECK = IElementType("DECK", TalonLanguage)
    @JvmField val GAMEPAD = IElementType("GAMEPAD", TalonLanguage)
    @JvmField val NOISE = IElementType("NOISE", TalonLanguage)
    @JvmField val PARROT = IElementType("PARROT", TalonLanguage)
    
    // Literals
    @JvmField val IDENTIFIER = IElementType("IDENTIFIER", TalonLanguage)
    @JvmField val NUMBER = IElementType("NUMBER", TalonLanguage)
    @JvmField val STRING = IElementType("STRING", TalonLanguage)
    @JvmField val WORD = IElementType("WORD", TalonLanguage)
    
    // Special
    @JvmField val WHITE_SPACE = TokenType.WHITE_SPACE
    @JvmField val BAD_CHARACTER = TokenType.BAD_CHARACTER
    @JvmField val NEW_LINE = IElementType("NEW_LINE", TalonLanguage)
}