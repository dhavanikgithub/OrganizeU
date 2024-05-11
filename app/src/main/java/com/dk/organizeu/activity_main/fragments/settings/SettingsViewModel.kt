package com.dk.organizeu.activity_main.fragments.settings

import androidx.lifecycle.ViewModel
import kotlin.properties.Delegates

class SettingsViewModel : ViewModel() {
    var uiMode:Int = 0
    var isStudent by Delegates.notNull<Boolean>()
}