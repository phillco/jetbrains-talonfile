package com.github.phillco.talon.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class TalonLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private var currentPosition: Int = 0
    private var tokenType: IElementType? = null
    private var tokenStartOffset: Int = 0
    private var tokenEndOffset: Int = 0
    
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.currentPosition = startOffset
        advance()
    }
    
    override fun getState(): Int = 0
    
    override fun getTokenType(): IElementType? = tokenType
    
    override fun getTokenStart(): Int = tokenStartOffset
    
    override fun getTokenEnd(): Int = tokenEndOffset
    
    override fun advance() {
        if (currentPosition >= endOffset) {
            tokenType = null
            return
        }
        
        tokenStartOffset = currentPosition
        
        when {
            // Skip whitespace (except newlines)
            buffer[currentPosition] in " \t\r" -> {
                while (currentPosition < endOffset && buffer[currentPosition] in " \t\r") {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.WHITE_SPACE
            }
            
            // Newlines
            buffer[currentPosition] == '\n' -> {
                currentPosition++
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.NEW_LINE
            }
            
            // Comments
            buffer[currentPosition] == '#' -> {
                while (currentPosition < endOffset && buffer[currentPosition] != '\n') {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.COMMENT
            }
            
            // Strings
            buffer[currentPosition] == '"' -> {
                currentPosition++
                while (currentPosition < endOffset && buffer[currentPosition] != '"') {
                    if (buffer[currentPosition] == '\\' && currentPosition + 1 < endOffset) {
                        currentPosition++
                    }
                    currentPosition++
                }
                if (currentPosition < endOffset) {
                    currentPosition++ // consume closing quote
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.STRING
            }
            
            // Numbers
            buffer[currentPosition].isDigit() -> {
                while (currentPosition < endOffset && (buffer[currentPosition].isDigit() || buffer[currentPosition] == '.')) {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.NUMBER
            }
            
            // Identifiers and keywords
            buffer[currentPosition].isLetter() || buffer[currentPosition] == '_' -> {
                val start = currentPosition
                while (currentPosition < endOffset && 
                       (buffer[currentPosition].isLetterOrDigit() || buffer[currentPosition] in "_.-")) {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                
                val text = buffer.substring(start, currentPosition)
                tokenType = when (text) {
                    "and" -> TalonTokenTypes.AND
                    "not" -> TalonTokenTypes.NOT
                    "or" -> TalonTokenTypes.OR
                    "settings" -> TalonTokenTypes.SETTINGS
                    "tag" -> TalonTokenTypes.TAG
                    "app" -> TalonTokenTypes.APP
                    "key" -> TalonTokenTypes.KEY
                    "face" -> TalonTokenTypes.FACE
                    "deck" -> TalonTokenTypes.DECK
                    "gamepad" -> TalonTokenTypes.GAMEPAD
                    "noise" -> TalonTokenTypes.NOISE
                    "parrot" -> TalonTokenTypes.PARROT
                    else -> TalonTokenTypes.IDENTIFIER
                }
            }
            
            // Single character tokens
            else -> {
                tokenType = when (buffer[currentPosition]) {
                    ':' -> TalonTokenTypes.COLON
                    '(' -> TalonTokenTypes.LPAREN
                    ')' -> TalonTokenTypes.RPAREN
                    '[' -> TalonTokenTypes.LBRACKET
                    ']' -> TalonTokenTypes.RBRACKET
                    '{' -> TalonTokenTypes.LBRACE
                    '}' -> TalonTokenTypes.RBRACE
                    ',' -> TalonTokenTypes.COMMA
                    '.' -> TalonTokenTypes.DOT
                    '|' -> TalonTokenTypes.PIPE
                    '-' -> TalonTokenTypes.DASH
                    '=' -> TalonTokenTypes.EQ
                    '+' -> TalonTokenTypes.PLUS
                    '*' -> TalonTokenTypes.STAR
                    '$' -> TalonTokenTypes.DOLLAR
                    '<' -> TalonTokenTypes.LESS
                    '>' -> TalonTokenTypes.GREATER
                    else -> TalonTokenTypes.BAD_CHARACTER
                }
                currentPosition++
                tokenEndOffset = currentPosition
            }
        }
    }
    
    override fun getBufferSequence(): CharSequence = buffer
    
    override fun getBufferEnd(): Int = endOffset
}