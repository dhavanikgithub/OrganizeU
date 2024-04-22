package com.dk.organizeu.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.utils.Constants.Companion.DATE_FORMAT_STRING
import com.dk.organizeu.utils.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.dk.organizeu.utils.Constants.Companion.TIME_FORMAT_STRING
import com.dk.organizeu.utils.UtilFunction.Companion.createNotificationChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UtilFunction {
    companion object{
        const val TAG = "OrganizeU-UtilFunction"
        val evenSemList = arrayOf(2,4,6,8)
        val oddSemList = arrayOf(1,3,5,7)

        val calendar = Calendar.getInstance()

        @SuppressLint("ConstantLocale")
        val dateFormat = SimpleDateFormat(DATE_FORMAT_STRING, Locale.getDefault())
        @SuppressLint("ConstantLocale")
        val timeFormat = SimpleDateFormat(TIME_FORMAT_STRING, Locale.getDefault())

        fun getCurrentDate():String
        {
            return  dateFormat.format(calendar.time)
        }

        fun getCurrentTime():String{
            return  timeFormat.format(calendar.time)
        }

        fun Context.showToast(message: String)
        {
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }

        fun Context.unexpectedErrorMessagePrint(e:Exception)
        {
            MainScope().launch(Dispatchers.Main)
            {
                showToast("Unexpected error happen: ${e.message}")
            }
        }

        fun checkLessonStatus(endTime: String): Boolean {

            try {
                val endDate = Calendar.getInstance().apply {
                    time = timeFormat.parse(endTime)!!
                    set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                }

                return endDate.timeInMillis>=System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
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

        fun isItemSelected(autoCompleteTextView: AutoCompleteTextView): Boolean {
            try {
                val selectedItem = autoCompleteTextView.text.toString().trim()
                val adapter = autoCompleteTextView.adapter as ArrayAdapter<String>
                for (i in 0 until adapter.count) {
                    if (selectedItem == adapter.getItem(i)) {
                        return true
                    }
                }
                return false
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun validateTime(startTimeString:String,endTimeString:String):Boolean {
            try {
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
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun Fragment.hideKeyboard() {
            try {
                view?.let { activity?.hideKeyboard(it) }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun Activity.hideKeyboard() {
            try {
                hideKeyboard(currentFocus ?: View(this))
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun Context.hideKeyboard(view: View) {
            try {
                val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }
        fun hideKeyboard(view: View) {
            try {
                val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
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
                                Log.e(NotificationScheduler.TAG, e.message.toString())
                                throw e
                            }
                        },
                        {
                            this.showToast("Permission is required allow in settings")
                        })
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
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
            try {
                val period = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                val minuteStr = if (minute < 10) "0$minute" else "$minute"
                return "$hour12:$minuteStr $period"
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun String.convert12HourTo24Hour(): String {
            val sdf12hr = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val sdf24hr = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf12hr.parse(this)
            return sdf24hr.format(date)
        }

        fun String.convert24HourTo12Hour(): String {
            val sdf24hr = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12hr = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf24hr.parse(this)
            return sdf12hr.format(date)
        }
    }
}