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

        fun validateEnrollment(enrollment: String): Boolean {
            // Check if the enrollment number is exactly 11 characters long
            if (enrollment.length != 11) {
                return false
            }

            // Check if all characters are digits
            if (!enrollment.all { it.isDigit() }) {
                return false
            }

            // If all conditions are satisfied, return true
            return true
        }

        fun validateStudentName(name: String): Boolean {
            // Check if the name is not empty and not longer than 50 characters
            if (name.isEmpty() || name.length > 50) {
                return false
            }

            // Check if the name contains only letters and spaces
            if (!name.all { it.isLetter() || it == ' ' }) {
                return false
            }

            // If all conditions are satisfied, return true
            return true
        }

        fun validatePassword(
            password: String,
            minPasswordLength: Int=6,
            maxPasswordLength: Int=20,
            requireAlphaChars: Boolean=true,
            minRequiredAlphaChars: Int=1,
            requireMixedCase: Boolean=true,
            requireNonAlphaChars: Boolean=true,
            minRequiredNonAlphaChars: Int=1,
            prohibitSpaceCharacter: Boolean=true
        ): Boolean {
            // Check if password length is within range
            if (password.length !in minPasswordLength..maxPasswordLength) {
                return false
            }

            // Check if password starts with prohibited characters
            val prohibitedStartingChars = listOf('#', '=', '~', '>', '<')
            if (password.isNotEmpty() && prohibitedStartingChars.contains(password.first())) {
                return false
            }

            // Check for alphabet characters if required
            if (requireAlphaChars && !password.any { it.isLetter() }) {
                return false
            }

            // Check for required minimum alphabet characters
            if (requireAlphaChars && password.count { it.isLetter() } < minRequiredAlphaChars) {
                return false
            }

            // Check for mixed case if required
            if (requireMixedCase && (password.none { it.isUpperCase() } || password.none { it.isLowerCase() })) {
                return false
            }

            // Check for non-alphabet characters if required
            if (requireNonAlphaChars && !password.any { it.isDigit() || it.isLetter() }) {
                return false
            }

            // Check for required minimum non-alphabet characters
            if (requireNonAlphaChars && password.count { it.isDigit() || it.isLetter() } < minRequiredNonAlphaChars) {
                return false
            }

            // Check for space character if prohibited
            if (prohibitSpaceCharacter && password.contains(' ')) {
                return false
            }

            // All conditions met, return true
            return true
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