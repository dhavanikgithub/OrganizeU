package com.dk.organizeu.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.utils.Constants.Companion.DATE_FORMAT_STRING
import com.dk.organizeu.utils.TimeConverter.Companion.timeFormat12H
import com.dk.organizeu.utils.TimeConverter.Companion.timeFormat24H
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UtilFunction {
    companion object{
        const val TAG = "OrganizeU-UtilFunction"
        val evenSemList = arrayOf(2,4,6,8)
        val oddSemList = arrayOf(1,3,5,7)

        val calendar = Calendar.getInstance()

        @SuppressLint("ConstantLocale")
        val dateFormat = SimpleDateFormat(DATE_FORMAT_STRING, Locale.getDefault())


        fun getCurrentDate():String
        {
            return  dateFormat.format(calendar.time)
        }

        fun getCurrentTime12H():String{
            return  timeFormat12H.format(calendar.time)
        }

        fun getCurrentTime24H():String{
            return  timeFormat24H.format(calendar.time)
        }

        fun Context.showToast(message: String)
        {
            MainScope().launch(Dispatchers.Main)
            {
                Toast.makeText(this@showToast,message,Toast.LENGTH_SHORT).show()
            }
        }

        fun String.containsOnlyAllowedCharacters(): Boolean {
            val regex = Regex("[^a-zA-Z0-9-_\\s]") // Regular expression to match any character except letters, digits, '-', and '_'
            return !regex.containsMatchIn(this)
        }

        fun String.isValidSubjectCode(): Boolean {
            val regex = Regex("^[a-zA-Z0-9_-]{5,15}$") // Regular expression to match subject code with length 5 to 15, containing only letters, digits, hyphens, and underscores
            return regex.matches(this)
        }


        fun Context.unexpectedErrorMessagePrint(e:Exception)
        {
            MainScope().launch(Dispatchers.Main)
            {
                showToast("Unexpected error happen: ${e.message}")
            }
        }


        fun showProgressBar(recyclerView: RecyclerView, progressBar: LinearLayout)
        {
            MainScope().launch(Dispatchers.Main)
            {
                recyclerView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
        }

        fun hideProgressBar(recyclerView: RecyclerView, progressBar: LinearLayout)
        {
            MainScope().launch(Dispatchers.Main)
            {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
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

        fun calculateLessonDuration(startTime: String, endTime: String): String {
            try {
                // Parse the start and end time strings
                val startTimeDate = timeFormat12H.parse(startTime)
                val endTimeDate = timeFormat12H.parse(endTime)

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


    }
}