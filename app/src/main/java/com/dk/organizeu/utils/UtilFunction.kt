package com.dk.organizeu.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UtilFunction {
    companion object{
        val evenSemList = arrayOf(2,4,6,8)
        val oddSemList = arrayOf(1,3,5,7)

        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.ENGLISH)

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

        fun isItemSelected(autoCompleteTextView: AutoCompleteTextView): Boolean {
            val selectedItem = autoCompleteTextView.text.toString().trim()
            val adapter = autoCompleteTextView.adapter as ArrayAdapter<String>
            for (i in 0 until adapter.count) {
                if (selectedItem == adapter.getItem(i)) {
                    return true
                }
            }
            return false
        }

        fun validateTime(startTimeString:String,endTimeString:String):Boolean {
            return if (startTimeString.isNotEmpty() && endTimeString.isNotEmpty()) {
                val startTime = timeFormat.parse(startTimeString)
                val endTime = timeFormat.parse(endTimeString)
                if (startTime != null && endTime != null) {
                    !endTime.before(startTime)
                } else{
                    false
                }
            } else{
                false
            }
        }

        fun Fragment.hideKeyboard() {
            view?.let { activity?.hideKeyboard(it) }
        }

        fun Activity.hideKeyboard() {
            hideKeyboard(currentFocus ?: View(this))
        }

        fun Context.hideKeyboard(view: View) {
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        fun hideKeyboard(view: View) {
            val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun calculateLessonDuration(startTime: String, endTime: String): String {

            try {
                // Parse the start and end time strings
                val startTimeDate = timeFormat.parse(startTime)
                val endTimeDate = timeFormat.parse(endTime)

                if (startTimeDate != null && endTimeDate != null) {
                    // Calculate the difference in milliseconds
                    val durationInMillis = endTimeDate.time - startTimeDate.time

                    // Convert milliseconds to minutes
                    val durationInMinutes = durationInMillis / (1000 * 60)

                    // Calculate hours and minutes from the total minutes
                    val hours = durationInMinutes / 60
                    val minutes = durationInMinutes % 60

                    // Return the formatted duration string
                    return String.format("%02d:%02d", hours, minutes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Return an empty string if there's any error
            return ""
        }
        fun convertTo12HourFormat(hour: Int, minute: Int): String {
            val period = if (hour < 12) "AM" else "PM"
            val hour12 = if (hour % 12 == 0) 12 else hour % 12
            val minuteStr = if (minute < 10) "0$minute" else "$minute"
            return "$hour12:$minuteStr $period"
        }

    }
}