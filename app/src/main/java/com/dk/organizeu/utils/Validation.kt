package com.dk.organizeu.utils

import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.dk.organizeu.utils.TimeConverter.Companion.timeFormat12H
import com.dk.organizeu.utils.TimeConverter.Companion.timeFormat24H
import java.util.Calendar

class Validation {
    companion object{
        fun checkLessonStatus(endTime: String): Boolean {

            try {
                val endDate = Calendar.getInstance().apply {
                    time = timeFormat24H.parse(endTime)!!
                    set(Calendar.YEAR, UtilFunction.calendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, UtilFunction.calendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, UtilFunction.calendar.get(Calendar.DAY_OF_MONTH))
                }

                return endDate.timeInMillis>=System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e(UtilFunction.TAG,e.message.toString())
                throw e
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
                Log.e(UtilFunction.TAG,e.message.toString())
                throw e
            }
        }

        fun validateTime(startTimeString:String,endTimeString:String):Boolean {
            try {
                return if (startTimeString.isNotEmpty() && endTimeString.isNotEmpty()) {
                    val startTime = timeFormat12H.parse(startTimeString)
                    val endTime = timeFormat12H.parse(endTimeString)
                    if (startTime != null && endTime != null) {
                        !endTime.before(startTime)
                    } else{
                        false
                    }
                } else{
                    false
                }
            } catch (e: Exception) {
                Log.e(UtilFunction.TAG,e.message.toString())
                throw e
            }
        }
    }
}