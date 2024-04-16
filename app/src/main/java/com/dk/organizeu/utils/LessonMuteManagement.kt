package com.dk.organizeu.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dk.organizeu.broadcast_receiver.LessonReminderReceiver
import java.util.*

class LessonMuteManagement {
    fun scheduleLessonAlarm(context: Context, lessonTime: String, action: String, requestCode: Int, lessonWeekday: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            this.action = action
        }

        // Check if the alarm already exists
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        if (pendingIntent != null) {
            cancelLessonAlarm(context, action, requestCode)
        }

        // Create a new PendingIntent
        val newPendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance()
        calendar.time = UtilFunction.timeFormat.parse(lessonTime)!!

        // Set the time of the lesson
        val lessonHour = calendar.get(Calendar.HOUR_OF_DAY)
        val lessonMinute = calendar.get(Calendar.MINUTE)

        // Calculate the next occurrence of the lesson based on the current day of the week and the specified lesson weekday
        val today = Calendar.getInstance()
        val daysUntilNextLesson = (lessonWeekday - today.get(Calendar.DAY_OF_WEEK) + 7) % 7
        val nextLessonDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, daysUntilNextLesson)
            set(Calendar.HOUR_OF_DAY, lessonHour)
            set(Calendar.MINUTE, lessonMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Set the alarm to repeat every week
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextLessonDate.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            newPendingIntent
        )
    }


    fun cancelLessonAlarm(context: Context, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun editLessonAlarm(context: Context, lessonTime: String, action: String, requestCode: Int) {
        // Cancel the existing alarm
        cancelLessonAlarm(context, action, requestCode)

        // Schedule a new alarm with the updated time
        scheduleLessonAlarm(context, lessonTime, action, requestCode)
    }

    fun scheduleLessonAlarm(context: Context, lessonTime: String, action: String, requestCode:Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            this.action = action
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        if (pendingIntent != null) {
            cancelLessonAlarm(context,action,requestCode)
        }

        val newPendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance()
        calendar.time = UtilFunction.timeFormat.parse(lessonTime)!!

        // Set calendar to today's date but with the time from lessonTime
        val now = Calendar.getInstance()
        calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

        // If the lesson time is in the past, schedule it for the next day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Set alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, newPendingIntent)
    }
}