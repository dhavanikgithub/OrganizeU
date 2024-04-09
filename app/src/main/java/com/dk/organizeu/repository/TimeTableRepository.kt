package com.dk.organizeu.repository

import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.repository.ClassRepository.Companion.classDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class TimeTableRepository {
    companion object{
        fun timetableCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): CollectionReference
        {
            return classDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId)
                .collection(FirebaseConfig.TIMETABLE_COLLECTION)
        }

        fun timetableDocumentRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): DocumentReference {
            return timetableCollectionRef(academicDocumentId,semesterDocumentId,classDocumentId).document(timetableDocumentId)
        }

        suspend fun getAllTimeTableDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): MutableList<DocumentSnapshot> {
            return timetableCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId).get().await().documents
        }

        suspend fun insertTimeTableDocument(
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