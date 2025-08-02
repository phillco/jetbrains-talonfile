package com.github.phillco.talon.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.github.phillco.talon.TalonFileType
import com.github.phillco.talon.TalonLanguage

class TalonFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TalonLanguage) {
    override fun getFileType(): FileType = TalonFileType
    
    override fun toString(): String = "Talon File"
}