package com.dk.organizeu.utils

import android.util.Log

class Logger {

    companion object {
        private const val TAG = "OrganizeU"

        fun v(tag: String = TAG, message: String) {
            Log.v(tag, message)
        }

        fun d(tag: String = TAG, message: String) {
            Log.d(tag, message)
        }

        fun i(tag: String = TAG, message: String) {
            Log.i(tag, message)
        }

        fun w(tag: String = TAG, message: String, tr:Throwable? = null) {
            Log.w(tag, message, tr)
        }

        fun e(tag: String = TAG, message: String, tr:Throwable? = null) {
            Log.e(tag, message,tr)
        }

        fun a(tag: String = TAG, message: String) {
            Log.wtf(tag, message)  // Log assert
        }
    }
}