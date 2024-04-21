package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.repository.AcademicRepository.Companion.academicDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class SemesterRepository {
    companion object{
        const val TAG = "OrganizeU-SemesterRepository"
        fun semesterCollectionRef(academicDocumentId: String): CollectionReference {
            try {
                return academicDocumentRef(academicDocumentId).collection(FirebaseConfig.SEMESTER_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun semesterDocumentRef(academicDocumentId: String, semesterDocumentId:String): DocumentReference {
            try {
                return semesterCollectionRef(academicDocumentId).document(semesterDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllSemesterDocuments(academicDocumentId: String): MutableList<DocumentSnapshot> {
            try {
                return semesterCollectionRef(academicDocumentId).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertSemesterDocuments(
            academicDocumentId: String,
            semesterDocumentId:String,
            inputHashMap:HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                semesterDocumentRef(academicDocumentId,semesterDocumentId).set(inputHashMap)
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
    }
}