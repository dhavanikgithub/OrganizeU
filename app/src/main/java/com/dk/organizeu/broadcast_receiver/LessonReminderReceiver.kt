package com.dk.organizeu.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class LessonReminderReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_START_LESSON = "ACTION_START_LESSON"
        const val ACTION_END_LESSON = "ACTION_END_LESSON"
        const val TAG = "OrganizeU-LessonReminderReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == ACTION_START_LESSON) {
                setDeviceMute(context, true)
            } else if (intent.action == ACTION_END_LESSON) {
                setDeviceMute(context, false)
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }
    private fun setDeviceMute(context: Context?, mute: Boolean) {
        try {
            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (mute) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }
}