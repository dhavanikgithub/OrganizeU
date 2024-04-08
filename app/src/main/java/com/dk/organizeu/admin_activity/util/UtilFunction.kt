package com.dk.organizeu.admin_activity.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UtilFunction {
    companion object{
        val evenSemList = arrayOf(2,4,6,8)
        val oddSemList = arrayOf(1,3,5,7)
        private val db = FirebaseFirestore.getInstance()

    }
}