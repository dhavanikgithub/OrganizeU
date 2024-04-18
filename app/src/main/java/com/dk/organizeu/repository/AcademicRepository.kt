package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AcademicRepository {
    companion object{
        val db = FirebaseFirestore.getInstance()

        fun academicCollectionRef(): CollectionReference {
            return db.collection(FirebaseConfig.ACADEMIC_COLLECTION)
        }

        fun academicDocumentRef(academicDocumentId:String): DocumentReference {
            return academicCollectionRef().document(academicDocumentId)
        }

        suspend fun getAllAcademicDocuments(): MutableList<DocumentSnapshot> {
            return academicCollectionRef().get().await().documents
        }

        fun insertAcademicDocuments(
            academicDocumentId: String,
            inputHashMap:HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                academicDocumentRef(academicDocumentId).set(inputHashMap)
                    .addOnSuccessListener {
                        successCallback(inputHashMap)
                    }
                    .addOnFailureListener {
                        failureCallback(it)
                    }

            }
            catch (e:java.lang.Exception)
            {
                failureCallback(e)
            }
        }

        suspend fun isAcademicDocumentExists(academicDocumentId: String): Boolean {
            return suspendCoroutine { continuation ->
                academicDocumentRef(academicDocumentId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        continuation.resume(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking academic document existence", exception)
                        continuation.resume(false)
                    }
            }
        }

        fun isAcademicDocumentExists(academicDocumentId: String, callback: (Boolean) -> Unit) {
            academicDocumentRef(academicDocumentId).get()
                .addOnSuccessListener { documentSnapshot ->
                    callback(documentSnapshot.exists())
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error checking academic document existence", exception)
                    callback(false)
                }
        }

        suspend fun deleteAcademicDocumentById(academicDocumentId: String){
            academicDocumentRef(academicDocumentId).delete().await()
        }

    }
}