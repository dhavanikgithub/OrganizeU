package com.dk.organizeu.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dk.organizeu.utils.Constants.Companion.ACTION_DELETE_NOTIFICATION
import com.dk.organizeu.utils.Constants.Companion.ACTION_EDIT_NOTIFICATION
import com.dk.organizeu.utils.NotificationScheduler

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_EDIT_NOTIFICATION) {
            // Handle editing notification
            val lessonName = intent.getStringExtra("lesson_name")
            // Reschedule the notification if needed
            // Here you can update the notification with new data or simply show it again
            NotificationScheduler.showNotification(context, lessonName ?: "Unknown Lesson")
        } else if (action == ACTION_DELETE_NOTIFICATION) {
            // Handle deleting notification
            NotificationScheduler.cancelNotification(context)
        }
    }
}