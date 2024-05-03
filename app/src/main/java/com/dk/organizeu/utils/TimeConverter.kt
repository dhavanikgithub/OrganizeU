package com.dk.organizeu.utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalTime
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

        fun calculateLessonDuration(startTime: String, endTime: String): String {
            try {
                // Parse the start and end time strings
                val timeFormat24H = SimpleDateFormat("HH:mm")
                val startTimeDate = timeFormat24H.parse(startTime)
                val endTimeDate = timeFormat24H.parse(endTime)

                if (startTimeDate != null && endTimeDate != null) {
                    // Calculate the difference in milliseconds
                    val durationInMillis = endTimeDate.time - startTimeDate.time

                    // Convert milliseconds to minutes
                    val durationInMinutes = durationInMillis / (1000 * 60)

                    // Return the formatted duration string
                    return formatDuration(durationInMinutes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Return an empty string if there's any error
            return ""
        }


        private fun formatDuration(durationInMinutes: Long): String {
            if (durationInMinutes < 60) {
                return "${durationInMinutes}m"
            }

            val hours = durationInMinutes / 60
            val minutes = durationInMinutes % 60

            return "${hours}h ${minutes}m"
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

        fun LocalTime.convert12HourTo24Hour(): String {
            val date = timeFormat12H.parse(this.toString())
            return timeFormat24H.format(date)
        }

        fun LocalTime.convert24HourTo12Hour(): String {
            val date = timeFormat24H.parse(this.toString())
            return timeFormat12H.format(date)
        }

    }
}