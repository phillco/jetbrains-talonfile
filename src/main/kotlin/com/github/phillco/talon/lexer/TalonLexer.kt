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
    private var inCommandPattern: Boolean = true  // Track if we're before or after colon
    private var lineStart: Boolean = true  // Track if we're at the start of a line
    
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.currentPosition = startOffset
        // Restore state from initialState
        this.inCommandPattern = (initialState and 1) == 0
        this.lineStart = (initialState and 2) != 0
        advance()
    }
    
    override fun getState(): Int {
        var state = 0
        if (!inCommandPattern) state = state or 1
        if (lineStart) state = state or 2
        return state
    }
    
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
                // Don't change lineStart flag for whitespace
            }
            
            // Newlines
            buffer[currentPosition] == '\n' -> {
                currentPosition++
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.NEW_LINE
                lineStart = true  // Mark that next token starts a line
            }
            
            // Comments
            buffer[currentPosition] == '#' -> {
                while (currentPosition < endOffset && buffer[currentPosition] != '\n') {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.COMMENT
                lineStart = false
            }
            
            // Separator lines (---)
            lineStart && buffer[currentPosition] == '-' && isAtSeparatorLine() -> {
                while (currentPosition < endOffset && buffer[currentPosition] == '-') {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.DASH
                inCommandPattern = true  // Reset state after separator
                lineStart = false
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
                lineStart = false
            }
            
            // Numbers
            buffer[currentPosition].isDigit() -> {
                while (currentPosition < endOffset && (buffer[currentPosition].isDigit() || buffer[currentPosition] == '.')) {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.NUMBER
                lineStart = false
            }
            
            // Captures and lists (<user.text>, <number>, {user.vocabulary})
            buffer[currentPosition] == '<' -> {
                currentPosition++
                while (currentPosition < endOffset && buffer[currentPosition] != '>' && buffer[currentPosition] != '\n') {
                    currentPosition++
                }
                if (currentPosition < endOffset && buffer[currentPosition] == '>') {
                    currentPosition++ // consume closing >
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.CAPTURE
                lineStart = false
            }
            
            // List references {user.vocabulary}
            buffer[currentPosition] == '{' && inCommandPattern -> {
                val start = currentPosition
                currentPosition++
                while (currentPosition < endOffset && buffer[currentPosition] != '}' && buffer[currentPosition] != '\n') {
                    currentPosition++
                }
                if (currentPosition < endOffset && buffer[currentPosition] == '}') {
                    currentPosition++ // consume closing }
                }
                tokenEndOffset = currentPosition
                // Check if it looks like a list reference (contains dots)
                val content = buffer.substring(start + 1, minOf(currentPosition - 1, endOffset))
                if (content.contains('.')) {
                    tokenType = TalonTokenTypes.LIST_REFERENCE
                } else {
                    // Not a list reference, just a regular brace
                    currentPosition = start + 1  // Reset to just after {
                    tokenEndOffset = currentPosition
                    tokenType = TalonTokenTypes.LBRACE
                }
                lineStart = false
            }
            
            // Regex patterns (^pattern$)
            buffer[currentPosition] == '^' -> {
                currentPosition++
                while (currentPosition < endOffset && buffer[currentPosition] != '$' && buffer[currentPosition] != '\n') {
                    if (buffer[currentPosition] == '\\' && currentPosition + 1 < endOffset) {
                        currentPosition++
                    }
                    currentPosition++
                }
                if (currentPosition < endOffset && buffer[currentPosition] == '$') {
                    currentPosition++ // consume closing $
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.REGEX
                lineStart = false
            }
            
            // Paths (starting with /)
            buffer[currentPosition] == '/' && currentPosition + 1 < endOffset && 
            (buffer[currentPosition + 1].isLetterOrDigit() || buffer[currentPosition + 1] in "._-") -> {
                currentPosition++
                while (currentPosition < endOffset && 
                       buffer[currentPosition] != '\n' && 
                       buffer[currentPosition] != '"' &&
                       buffer[currentPosition] != ')' &&
                       buffer[currentPosition] != ',') {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                tokenType = TalonTokenTypes.PATH
                lineStart = false
            }
            
            // Identifiers and keywords
            buffer[currentPosition].isLetter() || buffer[currentPosition] == '_' -> {
                // If we're at line start and see a non-whitespace character, reset to command pattern
                if (lineStart) {
                    inCommandPattern = true
                    lineStart = false
                }
                
                val start = currentPosition
                
                // Check if it's a function call or action (ends with parenthesis)
                var lookahead = currentPosition
                while (lookahead < endOffset && 
                       (buffer[lookahead].isLetterOrDigit() || buffer[lookahead] in "_.-")) {
                    lookahead++
                }
                
                val hasParenthesis = lookahead < endOffset && buffer[lookahead] == '('
                
                // Consume the identifier
                while (currentPosition < endOffset && 
                       (buffer[currentPosition].isLetterOrDigit() || buffer[currentPosition] in "_.-")) {
                    currentPosition++
                }
                tokenEndOffset = currentPosition
                
                val text = buffer.substring(start, currentPosition)
                
                // Check if it's a keyword first
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
                    "mode" -> TalonTokenTypes.MODE
                    "user" -> TalonTokenTypes.USER
                    "self" -> TalonTokenTypes.SELF
                    else -> {
                        when {
                            hasParenthesis -> {
                                // Check if it's a known action pattern
                                if (text.startsWith("user.") || text.startsWith("app.") || 
                                    text.startsWith("win.") || text.startsWith("edit.") ||
                                    text.startsWith("key.") || text.startsWith("mouse.") ||
                                    text == "insert" || text == "sleep" || text == "print") {
                                    TalonTokenTypes.ACTION_NAME
                                } else {
                                    TalonTokenTypes.FUNCTION_NAME
                                }
                            }
                            text.contains('.') -> TalonTokenTypes.VARIABLE_REFERENCE
                            inCommandPattern && !text.contains('.') -> TalonTokenTypes.WORD
                            else -> TalonTokenTypes.IDENTIFIER
                        }
                    }
                }
            }
            
            // Single character tokens
            else -> {
                tokenType = when (buffer[currentPosition]) {
                    ':' -> {
                        inCommandPattern = false  // After colon, we're in action part
                        lineStart = false
                        TalonTokenTypes.COLON
                    }
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
                    '^' -> TalonTokenTypes.CARET
                    '_' -> TalonTokenTypes.UNDERSCORE
                    else -> TalonTokenTypes.BAD_CHARACTER
                }
                currentPosition++
                tokenEndOffset = currentPosition
                lineStart = false
            }
        }
    }
    
    override fun getBufferSequence(): CharSequence = buffer
    
    override fun getBufferEnd(): Int = endOffset
    
    private fun isAtSeparatorLine(): Boolean {
        // Check if we're at the start of a line with 3+ dashes
        var pos = currentPosition
        var dashCount = 0
        
        // Count dashes
        while (pos < endOffset && buffer[pos] == '-') {
            dashCount++
            pos++
        }
        
        // Check if line ends after dashes (or has only whitespace)
        while (pos < endOffset && buffer[pos] in " \t\r") {
            pos++
        }
        
        return dashCount >= 3 && (pos >= endOffset || buffer[pos] == '\n')
    }
}