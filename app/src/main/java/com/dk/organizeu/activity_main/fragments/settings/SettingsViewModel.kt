package com.dk.organizeu.activity_main.fragments.settings

import androidx.lifecycle.ViewModel
import kotlin.properties.Delegates

class SettingsViewModel : ViewModel() {
    var isDarkMode:Boolean = false
    var isStudent by Delegates.notNull<Boolean>()
}