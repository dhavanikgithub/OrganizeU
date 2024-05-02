package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.pojo.TimetablePojo.Companion.toTimetablePojo
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

        suspend fun deleteTimetableDocument(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId: String){
            try {
                LessonRepository.deleteAllLessonDocuments(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
                timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllTimetableDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String){
            try {
                getAllTimeTableDocuments(academicDocumentId, semesterDocumentId, classDocumentId).map {
                    deleteTimetableDocument(academicDocumentId,semesterDocumentId,classDocumentId,it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertTimeTableDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            timetablePojo: TimetablePojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
        ) {
            try {
                timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetablePojo.id)
                    .set(timetablePojo)
                    .addOnSuccessListener {
                        successCallback(true)
                    }.addOnFailureListener {
                        failureCallback(it)
                    }

            } catch (e: Exception) {
                failureCallback(e)
            }
        }

        suspend fun insertTimeTableDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            timetablePojo: TimetablePojo,
        ) {
            timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetablePojo.id)
                .set(timetablePojo)
                .await()
        }

        suspend fun getTimetablePojoByName(academicDocumentId: String,semesterDocumentId: String, classDocumentId: String,name:String): TimetablePojo? {
            return try {
                timetableCollectionRef(
                    academicDocumentId,
                    semesterDocumentId,
                    classDocumentId
                ).whereEqualTo("name",name)
                    .get()
                    .await()
                    .documents[0].toTimetablePojo()
            } catch (e: Exception) {
                null
            }
        }

        suspend fun getTimetableIdByName(academicDocumentId: String,semesterDocumentId: String, classDocumentId: String,name:String): String? {
            return try {
                getTimetablePojoByName(academicDocumentId, semesterDocumentId, classDocumentId, name)!!.id
            } catch (e: Exception) {
                null
            }
        }
    }
}