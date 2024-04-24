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
        const val TAG = "OrganizeU-AcademicRepository"

        fun academicCollectionRef(): CollectionReference {
            try {
                return db.collection(FirebaseConfig.ACADEMIC_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun academicDocumentRef(academicDocumentId:String): DocumentReference {
            try {
                return academicCollectionRef().document(academicDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllAcademicDocuments(): MutableList<DocumentSnapshot> {
            try {
                return academicCollectionRef().get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
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
            try {
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
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isAcademicDocumentExists(academicDocumentId: String, callback: (Boolean) -> Unit) {
            try {
                academicDocumentRef(academicDocumentId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        callback(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking academic document existence", exception)
                        callback(false)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAcademicDocument(academicDocumentId: String){
            try {
                SemesterRepository.deleteAllSemesterDocuments(academicDocumentId)
                academicDocumentRef(academicDocumentId).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllAcademicDocuments(){
            try {
                getAllAcademicDocuments().map {
                    deleteAcademicDocument(it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

    }
}