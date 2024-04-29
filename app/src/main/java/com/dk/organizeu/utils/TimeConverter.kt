package com.dk.organizeu.utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TimeConverter {
    companion object{
        @SuppressLint("ConstantLocale")
        val timeFormat12H = SimpleDateFormat(Constants.TIME_FORMAT_12H_STRING, Locale.getDefault())
        @SuppressLint("ConstantLocale")
        val timeFormat24H = SimpleDateFormat(Constants.TIME_FORMAT_24H_STRING, Locale.getDefault())

        fun millisecondsTo12HourFormat(milliseconds: Long): String {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = milliseconds
            }
            return timeFormat12H.format(calendar.time)
        }

        fun convertTo12HourFormat(hour: Int, minute: Int): String {
            try {
                val period = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                val minuteStr = if (minute < 10) "0$minute" else "$minute"
                return "$hour12:$minuteStr $period"
            } catch (e: Exception) {
                Log.e(UtilFunction.TAG,e.message.toString())
                throw e
            }
        }

        fun String.convert12HourTo24Hour(): String {
            val date = timeFormat12H.parse(this)
            return timeFormat24H.format(date)
        }

        fun String.convert24HourTo12Hour(): String {
            val date = timeFormat24H.parse(this)
            return timeFormat12H.format(date)
        }

    }
}