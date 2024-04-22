package com.dk.organizeu.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dk.organizeu.R
import com.dk.organizeu.broadcast_receiver.NotificationReceiver
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.utils.Constants.Companion.ACTION_EDIT_NOTIFICATION
import com.dk.organizeu.utils.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.dk.organizeu.utils.Constants.Companion.NOTIFICATION_ID
import com.dk.organizeu.utils.Constants.Companion.NOTIFICATION_PERMISSION
import com.dk.organizeu.utils.PermissionManager.Companion.isNotificationPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.requestPermission
import com.dk.organizeu.utils.UtilFunction.Companion.createNotificationChannel
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.timeFormat
import java.text.SimpleDateFormat
import java.util.*

class NotificationScheduler {
    companion object {
        const val TAG = "OrganizeU-NotificationScheduler"
        fun scheduleNotification(context: Context, lessonName: String, startTimeStr: String) {
            try {
                context.createNotificationChannel("Lesson Notifications","Channel for lesson notifications",NOTIFICATION_CHANNEL_ID)
                cancelNotification(context)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, NotificationReceiver::class.java)
                intent.putExtra("lesson_name", lessonName)
                intent.putExtra("notification_id", NOTIFICATION_ID)
                intent.action = ACTION_EDIT_NOTIFICATION // This action indicates editing the notification
                val pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                val calendar = Calendar.getInstance()
                calendar.time = timeFormat.parse(startTimeStr)!!

                // Set calendar to today's date but with the time from lessonTime
                val now = Calendar.getInstance()
                calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

                // If the lesson time is in the past, schedule it for the next day
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun scheduleNotification(context: Context, lessonName: String, startTimeStr: String, weekday: Int, notificationId:Int) {
            try {
                cancelNotification(context)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, NotificationReceiver::class.java)
                intent.putExtra("lesson_name", lessonName)
                intent.putExtra("notification_id", notificationId)
                intent.action = ACTION_EDIT_NOTIFICATION // This action indicates editing the notification
                val pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                // Parse the start time
                val calendar = Calendar.getInstance()
                calendar.time = timeFormat.parse(startTimeStr)!!

                // Set calendar to today's date but with the time from lessonTime
                val now = Calendar.getInstance()
                calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

                // If the lesson time is in the past, schedule it for the next weekday
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                // Set calendar to the next occurrence of the provided weekday
                while (calendar.get(Calendar.DAY_OF_WEEK) != weekday) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling notification: ${e.message}")
                e.printStackTrace()
            }
        }

        fun cancelNotification(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, NotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.cancel(pendingIntent)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun showNotification(context: Context, lessonName: String) {
            try {
                if(isNotificationPermissionGranted(context))
                {
                    val intent = Intent(context, StudentActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE)

                    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Upcoming Lesson")
                        .setContentText("Your lesson '$lessonName' is about to start.")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                }
                else{
                    requestPermission(context, NOTIFICATION_PERMISSION,{
                        try {
                            showNotification(context,lessonName)
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            throw e
                        }
                    },{
                        context.showToast("Permission is required allow in settings")
                    })
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }
    }
}