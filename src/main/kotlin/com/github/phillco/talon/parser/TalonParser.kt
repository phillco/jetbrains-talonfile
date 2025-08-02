package com.github.phillco.talon.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.github.phillco.talon.lexer.TalonTokenTypes

class TalonParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        
        while (!builder.eof()) {
            if (!parseTopLevel(builder)) {
                builder.advanceLexer()
            }
        }
        
        rootMarker.done(root)
        return builder.treeBuilt
    }
    
    private fun parseTopLevel(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        
        return when {
            // Parse context matches (e.g., "app: chrome")
            parseMatch(builder) -> {
                marker.done(TalonElementTypes.MATCH)
                true
            }
            
            // Parse commands (e.g., "hello world: insert('Hello, World!')")
            parseCommand(builder) -> {
                marker.done(TalonElementTypes.COMMAND)
                true
            }
            
            // Parse bindings (e.g., "settings():", "tag():")
            parseBinding(builder) -> {
                marker.done(TalonElementTypes.BINDING)
                true
            }
            
            else -> {
                marker.drop()
                false
            }
        }
    }
    
    private fun parseMatch(builder: PsiBuilder): Boolean {
        val start = builder.mark()
        
        // Look for pattern: [modifiers] identifier: value
        var hasModifiers = false
        while (builder.tokenType in listOf(TalonTokenTypes.AND, TalonTokenTypes.NOT)) {
            builder.advanceLexer()
            hasModifiers = true
        }
        
        if (builder.tokenType != TalonTokenTypes.IDENTIFIER) {
            start.rollbackTo()
            return false
        }
        
        builder.advanceLexer()
        
        if (builder.tokenType != TalonTokenTypes.COLON) {
            start.rollbackTo()
            return false
        }
        
        builder.advanceLexer()
        
        // Parse the rest of the line as the match value
        while (!builder.eof() && builder.tokenType != TalonTokenTypes.NEW_LINE) {
            builder.advanceLexer()
        }
        
        if (builder.tokenType == TalonTokenTypes.NEW_LINE) {
            builder.advanceLexer()
        }
        
        // Check if this is followed by a separator line (----)
        if (isAtSeparatorLine(builder)) {
            while (!builder.eof() && builder.tokenType == TalonTokenTypes.DASH) {
                builder.advanceLexer()
            }
            if (builder.tokenType == TalonTokenTypes.NEW_LINE) {
                builder.advanceLexer()
            }
        }
        
        start.drop()
        return true
    }
    
    private fun parseCommand(builder: PsiBuilder): Boolean {
        val start = builder.mark()
        
        // Parse rule (command pattern)
        if (!parseRule(builder)) {
            start.rollbackTo()
            return false
        }
        
        if (builder.tokenType != TalonTokenTypes.COLON) {
            start.rollbackTo()
            return false
        }
        
        builder.advanceLexer()
        
        // Parse statements (command actions)
        parseStatements(builder)
        
        start.drop()
        return true
    }
    
    private fun parseBinding(builder: PsiBuilder): Boolean {
        val start = builder.mark()
        
        // Look for patterns like "settings():", "tag():", etc.
        val bindingKeywords = listOf(
            TalonTokenTypes.SETTINGS, TalonTokenTypes.TAG, TalonTokenTypes.APP,
            TalonTokenTypes.KEY, TalonTokenTypes.FACE, TalonTokenTypes.DECK,
            TalonTokenTypes.GAMEPAD, TalonTokenTypes.NOISE, TalonTokenTypes.PARROT
        )
        
        if (builder.tokenType !in bindingKeywords) {
            start.rollbackTo()
            return false
        }
        
        builder.advanceLexer()
        
        // Expect parentheses
        if (builder.tokenType == TalonTokenTypes.LPAREN) {
            builder.advanceLexer()
            
            // Parse parameters inside parentheses
            while (!builder.eof() && builder.tokenType != TalonTokenTypes.RPAREN) {
                builder.advanceLexer()
            }
            
            if (builder.tokenType == TalonTokenTypes.RPAREN) {
                builder.advanceLexer()
            }
        }
        
        if (builder.tokenType != TalonTokenTypes.COLON) {
            start.rollbackTo()
            return false
        }
        
        builder.advanceLexer()
        
        // Parse the body
        parseStatements(builder)
        
        start.drop()
        return true
    }
    
    private fun parseRule(builder: PsiBuilder): Boolean {
        // Simple rule parsing - just consume tokens until we hit a colon
        var hasContent = false
        
        while (!builder.eof() && builder.tokenType != TalonTokenTypes.COLON && builder.tokenType != TalonTokenTypes.NEW_LINE) {
            builder.advanceLexer()
            hasContent = true
        }
        
        return hasContent
    }
    
    private fun parseStatements(builder: PsiBuilder) {
        // Skip to next line if needed
        if (builder.tokenType == TalonTokenTypes.NEW_LINE) {
            builder.advanceLexer()
        }
        
        // Parse indented block or single line statement
        while (!builder.eof()) {
            // Check if we're at the start of a new top-level construct
            if (isAtTopLevel(builder)) {
                break
            }
            
            // Skip empty lines
            if (builder.tokenType == TalonTokenTypes.NEW_LINE) {
                builder.advanceLexer()
                continue
            }
            
            // Parse statement
            val stmtMarker = builder.mark()
            while (!builder.eof() && builder.tokenType != TalonTokenTypes.NEW_LINE) {
                builder.advanceLexer()
            }
            stmtMarker.done(TalonElementTypes.STATEMENT)
            
            if (builder.tokenType == TalonTokenTypes.NEW_LINE) {
                builder.advanceLexer()
            }
        }
    }
    
    private fun isAtSeparatorLine(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        var dashCount = 0
        
        while (!builder.eof() && builder.tokenType == TalonTokenTypes.DASH) {
            dashCount++
            builder.advanceLexer()
        }
        
        val result = dashCount >= 3 && (builder.eof() || builder.tokenType == TalonTokenTypes.NEW_LINE)
        marker.rollbackTo()
        return result
    }
    
    private fun isAtTopLevel(builder: PsiBuilder): Boolean {
        // Simple heuristic: if we see a keyword or identifier followed by colon at start of line
        val marker = builder.mark()
        
        // Skip any leading whitespace
        while (builder.tokenType == TalonTokenTypes.WHITE_SPACE) {
            builder.advanceLexer()
        }
        
        val result = when (builder.tokenType) {
            TalonTokenTypes.SETTINGS, TalonTokenTypes.TAG, TalonTokenTypes.APP,
            TalonTokenTypes.KEY, TalonTokenTypes.FACE, TalonTokenTypes.DECK,
            TalonTokenTypes.GAMEPAD, TalonTokenTypes.NOISE, TalonTokenTypes.PARROT,
            TalonTokenTypes.AND, TalonTokenTypes.NOT -> true
            
            TalonTokenTypes.IDENTIFIER -> {
                // Could be a match or command start
                true
            }
            
            else -> false
        }
        
        marker.rollbackTo()
        return result
    }
}