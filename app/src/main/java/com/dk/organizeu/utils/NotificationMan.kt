package com.dk.organizeu.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dk.organizeu.R
import com.dk.organizeu.utils.UtilFunction.Companion.showToast

class NotificationMan {

    companion object{
        const val TAG = "OrganizeU-LessonNotificationManager"
    }
    fun Context.createNotificationChannel(channelName:String,channelDescription:String, channelId:String) {
        try {
            if(PermissionManager.isNotificationPermissionGranted(this))
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val existingChannel = notificationManager.getNotificationChannel(channelId)
                    if (existingChannel == null) {
                        /*val name = "Lesson Notifications"
                        val descriptionText = "Channel for lesson notifications"*/
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val channel = NotificationChannel(channelId, channelName, importance).apply {
                            description = channelDescription
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }
            }
            else{
                PermissionManager.requestPermission(
                    this,
                    Constants.NOTIFICATION_PERMISSION,
                    {
                        try {
                            this.createNotificationChannel(channelName,channelDescription,channelId)
                        } catch (e: Exception) {
                            Log.e(UtilFunction.TAG, e.message.toString())
                            throw e
                        }
                    },
                    {
                        this.showToast("Permission is required allow in settings")
                    })
            }
        } catch (e: Exception) {
            Log.e(UtilFunction.TAG,e.message.toString())
            throw e
        }
    }
    fun showNotification(context: Context, lessonName: String) {
        try {
            if(PermissionManager.isNotificationPermissionGranted(context))
            {
                /*val intent = Intent(context, StudentActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE)

                val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Upcoming Lesson")
                    .setContentText("Your lesson '$lessonName' is about to start.")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)*/

                val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Upcoming Lesson")
                    .setContentText("Your lesson '$lessonName' is about to start.")
                    .setAutoCancel(true)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(Constants.NOTIFICATION_ID, builder.build())
            }
            else{
                PermissionManager.requestPermission(context, Constants.NOTIFICATION_PERMISSION, {
                    try {
                        showNotification(context, lessonName)
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                        throw e
                    }
                }, {
                    context.showToast("Permission is required allow in settings")
                })
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            throw e
        }
    }
}