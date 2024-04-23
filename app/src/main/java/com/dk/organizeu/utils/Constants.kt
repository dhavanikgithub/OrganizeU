package com.dk.organizeu.utils

class Constants {
    companion object{
        const val NOTIFICATION_PERMISSION = android.Manifest.permission.POST_NOTIFICATIONS
        const val AUDIO_SETTING_PERMISSION = android.Manifest.permission.MODIFY_AUDIO_SETTINGS
        const val NOTIFICATION_CHANNEL_ID = "lesson_notification_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_EDIT_NOTIFICATION = "edit_notification"
        const val ACTION_DELETE_NOTIFICATION = "delete_notification"

        const val DATE_FORMAT_STRING = "dd-MM-yyyy"

        const val TIME_FORMAT_12H_STRING = "hh:mm a"
        const val TIME_FORMAT_24H_STRING = "HH:mm"
    }
}