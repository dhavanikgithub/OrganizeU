package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig.Companion.WEEKDAY_COLLECTION
import com.dk.organizeu.firebase.key_mapping.TimeTableCollection
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.utils.TimeConverter.Companion.convert24HourTo12Hour
import com.dk.organizeu.utils.TimeConverter.Companion.timeFormat12H
import com.google.firebase.firestore.CollectionReference
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
            timetableDocumentId:String,
            inputHashMap: HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
            ){
            try {
                val myHashMap = hashMapOf(
                    TimeTableCollection.WEEKDAY.displayName to timetableDocumentId
                )
                TimeTableRepository.insertTimeTableDocument(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId, myHashMap,
                    {
                        try {
                            lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
                                .document()
                                .set(inputHashMap)
                                .addOnSuccessListener {
                                    successCallback(inputHashMap)
                                }
                                .addOnFailureListener {
                                    failureCallback(it)
                                }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            throw e
                        }
                    }, {
                        Log.e(TAG,it.message.toString())
                        throw it
                    })
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }

        }

        fun lessonDocumentToLessonObj(document: DocumentSnapshot,counter:Int): TimetablePojo {
            try {
                return TimetablePojo(
                    document.get(WeekdayCollection.CLASS_NAME.displayName).toString(),
                    document.get(WeekdayCollection.SUBJECT_NAME.displayName).toString(),
                    document.get(WeekdayCollection.SUBJECT_CODE.displayName).toString(),
                    document.get(WeekdayCollection.LOCATION.displayName).toString(),
                    document.get(WeekdayCollection.START_TIME.displayName).toString().convert24HourTo12Hour(),
                    document.get(WeekdayCollection.END_TIME.displayName).toString().convert24HourTo12Hour(),
                    document.get(WeekdayCollection.DURATION.displayName).toString(),
                    document.get(WeekdayCollection.TYPE.displayName).toString(),
                    document.get(WeekdayCollection.FACULTY_NAME.displayName).toString(),
                    counter,
                    document.get(WeekdayCollection.MUTE_REQUEST_CODE.displayName).toString().toInt(),
                    document.get(WeekdayCollection.UNMUTE_REQUEST_CODE.displayName).toString().toInt(),
                    document.get(WeekdayCollection.NOTIFICATION_CODE.displayName).toString().toInt()
                )
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