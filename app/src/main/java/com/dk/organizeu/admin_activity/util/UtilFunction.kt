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
        suspend fun isAcademicDocumentExists(academicDocumentId: String): Boolean {
            return suspendCoroutine { continuation ->
                db.collection("academic")
                    .document(academicDocumentId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        continuation.resume(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking academic document existence", exception)
                        continuation.resume(false)
                    }
            }
        }

        private fun isAcademicDocumentExists(academicDocumentId: String, callback: (Boolean) -> Unit) {
            db.collection("academic")
                .document(academicDocumentId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    callback(documentSnapshot.exists())
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error checking academic document existence", exception)
                    callback(false)
                }
        }
    }
}