package com.github.phillco.talon

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object TalonFileType : LanguageFileType(TalonLanguage) {
    override fun getName(): String = "Talon"
    
    override fun getDescription(): String = "Talon language file"
    
    override fun getDefaultExtension(): String = "talon"
    
    override fun getIcon(): Icon = TalonIcons.FILE
}