package com.dk.organizeu.main_activity.fragments

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SplashViewModel : ViewModel() {
    suspend fun delayAndNavigate(delayMillis: Long) {
        withContext(Dispatchers.IO) {
            delay(delayMillis)
        }
    }
}