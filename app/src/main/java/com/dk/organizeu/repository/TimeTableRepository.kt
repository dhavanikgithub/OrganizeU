package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.repository.ClassRepository.Companion.classDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class TimeTableRepository {
    companion object{
        const val TAG = "OrganizeU-TimeTableRepository"
        fun timetableCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): CollectionReference
        {
            try {
                return classDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId).collection(FirebaseConfig.TIMETABLE_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun timetableDocumentRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): DocumentReference {
            try {
                return timetableCollectionRef(academicDocumentId,semesterDocumentId,classDocumentId).document(timetableDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllTimeTableDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): MutableList<DocumentSnapshot> {
            try {
                return timetableCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId)
                    .get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertTimeTableDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            timetableDocumentId: String,
            inputHashMap: HashMap<String, String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ) {
            try {
                timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
                    .set(inputHashMap)
                    .addOnSuccessListener {
                        successCallback(inputHashMap)
                    }.addOnFailureListener {
                        failureCallback(it)
                    }

            } catch (e: Exception) {
                failureCallback(e)
            }
        }
    }
}