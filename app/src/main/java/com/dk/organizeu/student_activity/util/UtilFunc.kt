package com.dk.organizeu.student_activity.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UtilFunc {

    companion object{
        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy",Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("hh:mm",Locale.ENGLISH)

        fun getCurrentDate():String
        {
            return  dateFormat.format(calendar.time)
        }

        fun getCurrentTime():String{
            return  timeFormat.format(calendar.time)
        }

        fun checkLessonStatus(startTime: String, endTime: String): Boolean {

            val startDate = Calendar.getInstance().apply {
                time = timeFormat.parse(startTime)!!
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            }
            val endDate = Calendar.getInstance().apply {
                time = timeFormat.parse(endTime)!!
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            }

            return when {

                calendar.before(startDate) -> true
                calendar.after(endDate) -> false
                else -> true
            }
        }

        fun getDayOfWeek(dayNumber: Int): String {
            return when (dayNumber) {
                0 -> "Mon"
                1 -> "Tue"
                2 -> "Wed"
                3 -> "Thu"
                4 -> "Fri"
                5 -> "Sat"
                6 -> "Sun"
                else -> throw IllegalArgumentException("Invalid day number: $dayNumber")
            }
        }

    }
}