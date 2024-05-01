package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig.Companion.WEEKDAY_COLLECTION
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.pojo.LessonPojo
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.utils.TimeConverter.Companion.timeFormat12H
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class LessonRepository {
    companion object{
        const val TAG = "OrganizeU-LessonRepository"
        fun lessonCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): CollectionReference {
            try {
                return TimeTableRepository.timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).collection(WEEKDAY_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }
        fun lessonDocumentRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId: String,lessonDocumentId: String): DocumentReference {
            try {
                return lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).document(lessonDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllLessonDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): MutableList<DocumentSnapshot> {
            try {
                return lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllLessonDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String,orderBy:String): MutableList<DocumentSnapshot> {
            try {
                return lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).orderBy(orderBy).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertLessonDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            timetablePojo: TimetablePojo,
            lessonPojo: LessonPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
            ){
            try {
                try {
                    lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetablePojo.id)
                        .document(lessonPojo.id)
                        .set(lessonPojo)
                        .addOnSuccessListener {
                            successCallback(true)
                        }
                        .addOnFailureListener {
                            failureCallback(it)
                        }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    throw e
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }

        }

        suspend fun deleteLessonDocument(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, timetableDocumentId: String, id:String)
        {
            try {
                lessonDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId, id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllLessonDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId: String)
        {
            try {
                getAllLessonDocuments(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).map {
                    deleteLessonDocument(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }



        fun isLessonDocumentExistsById(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, timetableDocumentId: String, id: String, callback: (Boolean) -> Unit){
            try {
                lessonDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId, id)
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

        suspend fun isLessonDocumentConflict(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, timetableDocumentId:String, startTime:String, endTime:String, facultyName:String, roomName:String, callback: (Boolean) -> Unit)
        {
            try {
                val lessonStartTime = timeFormat12H.parse(startTime)
                val lessonEndTime = timeFormat12H.parse(endTime)
                val semesterDocuments = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
                for(semesterDoc in semesterDocuments)
                {
                    try {
                        val classDocuments = ClassRepository.getAllClassDocuments(academicDocumentId, semesterDoc.id)
                        for (classDoc in classDocuments)
                        {
                            try {
                                val lessonDocuments = getAllLessonDocuments(academicDocumentId, semesterDoc.id, classDoc.id, timetableDocumentId)
                                for(lessonDoc in lessonDocuments)
                                {
                                    try {
                                        val parsedStartTime = timeFormat12H.parse(lessonDoc.get(WeekdayCollection.START_TIME.displayName).toString())
                                        val parsedEndTime = timeFormat12H.parse(lessonDoc.get(WeekdayCollection.END_TIME.displayName).toString())
                                        if (parsedStartTime != null) {
                                            if (parsedEndTime != null) {
                                                if (parsedStartTime.before(lessonEndTime) && parsedEndTime.after(lessonStartTime)) {
                                                    if (semesterDoc.id != semesterDocumentId)
                                                    {
                                                        if (facultyName == (lessonDoc.get(WeekdayCollection.FACULTY_NAME.displayName).toString()))
                                                        {
                                                            callback(true)
                                                            return
                                                        }
                                                        else if(roomName == (lessonDoc.get(WeekdayCollection.LOCATION.displayName)))
                                                        {
                                                            callback(true)
                                                            return
                                                        }
                                                    }
                                                    else{
                                                        callback(true)
                                                        return
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG,e.message.toString())
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG,e.message.toString())
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                    }
                }
                callback(false)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                callback(false)
                throw e
            }
        }
    }
}