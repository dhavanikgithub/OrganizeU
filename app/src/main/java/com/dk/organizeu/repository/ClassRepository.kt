package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.repository.SemesterRepository.Companion.semesterDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class ClassRepository {
    companion object{
        const val TAG = "OrganizeU-ClassRepository"
        fun classCollectionRef(academicDocumentId: String, semesterDocumentId: String): CollectionReference {
            try {
                return semesterDocumentRef(academicDocumentId,semesterDocumentId).collection(
                    FirebaseConfig.CLASS_COLLECTION
                )
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun classDocumentRef(academicDocumentId: String, semesterDocumentId: String, classDocumentId:String): DocumentReference {
            try {
                return classCollectionRef(academicDocumentId,semesterDocumentId).document(classDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllClassDocuments(academicDocumentId: String,semesterDocumentId: String): MutableList<DocumentSnapshot> {
            try {
                return classCollectionRef(academicDocumentId, semesterDocumentId).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteClassDocument(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String){
            try {
                BatchRepository.deleteAllBatchDocuments(academicDocumentId, semesterDocumentId, classDocumentId)
                TimeTableRepository.deleteAllTimetableDocuments(academicDocumentId, semesterDocumentId, classDocumentId)
                classDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllClassDocuments(academicDocumentId: String,semesterDocumentId: String)
        {
            try {
                getAllClassDocuments(academicDocumentId, semesterDocumentId).map {
                    deleteClassDocument(academicDocumentId,semesterDocumentId,it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertClassDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            inputHashMap:HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                classDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId).set(inputHashMap)
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

        fun isClassDocumentExists(academicDocumentId: String,semesterDocumentId: String,classDocumentId:String, callback: (Boolean) -> Unit) {
            try {
                classDocumentRef(academicDocumentId,semesterDocumentId, classDocumentId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        callback(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        callback(false) // Assume document doesn't exist if there's an error
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

    }
}