package com.dk.organizeu.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class LessonReminderReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_START_LESSON = "ACTION_START_LESSON"
        const val ACTION_END_LESSON = "ACTION_END_LESSON"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_START_LESSON) {
            setDeviceMute(context, true)
        } else if (intent.action == ACTION_END_LESSON) {
            setDeviceMute(context, false)
        }
    }
    private fun setDeviceMute(context: Context?, mute: Boolean) {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (mute) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
    }
}