package com.dk.organizeu.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {

    // Function to get SharedPreferences instance
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }



    // Function to add a key-value pair to SharedPreferences
    fun addValue(context: Context, key: String, value: Any) {
        val editor = getSharedPreferences(context).edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Boolean -> editor.putBoolean(key, value)
            else -> throw IllegalArgumentException("Unsupported value type")
        }
        editor.apply()
    }

    // Function to update the value of a key in SharedPreferences
    fun updateValue(context: Context, key: String, value: Any) {
        if (containsKey(context, key)) {
            addValue(context, key, value)
        } else {
            throw IllegalArgumentException("Key '$key' does not exist in SharedPreferences")
        }
    }

    // Function to remove a key-value pair from SharedPreferences
    fun removeValue(context: Context, key: String) {
        getSharedPreferences(context).edit().remove(key).apply()
    }

    // Function to check if a key exists in SharedPreferences
    fun containsKey(context: Context, key: String): Boolean {
        return getSharedPreferences(context).contains(key)
    }

    // Function to clear all data from SharedPreferences
    fun clearSharedPreferences(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }

    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return getSharedPreferences(context).getString(key, defaultValue) ?: defaultValue
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getSharedPreferences(context).getInt(key, defaultValue)
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        return getSharedPreferences(context).getLong(key, defaultValue)
    }

    fun getFloat(context: Context, key: String, defaultValue: Float = 0f): Float {
        return getSharedPreferences(context).getFloat(key, defaultValue)
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }
}