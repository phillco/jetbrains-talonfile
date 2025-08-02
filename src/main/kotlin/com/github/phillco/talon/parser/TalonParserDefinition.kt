package com.github.phillco.talon.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.github.phillco.talon.TalonLanguage
import com.github.phillco.talon.lexer.TalonLexer
import com.github.phillco.talon.lexer.TalonTokenTypes
import com.github.phillco.talon.psi.TalonFile

class TalonParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer = TalonLexer()
    
    override fun getCommentTokens(): TokenSet = TokenSet.create(TalonTokenTypes.COMMENT)
    
    override fun getStringLiteralElements(): TokenSet = TokenSet.create(TalonTokenTypes.STRING)
    
    override fun createParser(project: Project): PsiParser = TalonParser()
    
    override fun getFileNodeType(): IFileElementType = FILE
    
    override fun createFile(viewProvider: FileViewProvider): PsiFile = TalonFile(viewProvider)
    
    override fun createElement(node: ASTNode): PsiElement = TalonElementTypes.Factory.createElement(node)
    
    companion object {
        val FILE = IFileElementType(TalonLanguage)
        val WHITE_SPACES = TokenSet.create(TalonTokenTypes.WHITE_SPACE)
        val COMMENTS = TokenSet.create(TalonTokenTypes.COMMENT)
        val STRINGS = TokenSet.create(TalonTokenTypes.STRING)
    }
}