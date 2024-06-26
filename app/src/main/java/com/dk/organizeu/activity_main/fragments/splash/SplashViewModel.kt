package com.dk.organizeu.activity_main.fragments.splash

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