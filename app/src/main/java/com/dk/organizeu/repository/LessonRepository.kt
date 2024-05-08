package com.dk.organizeu.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.dk.organizeu.firebase.FirebaseConfig.Companion.WEEKDAY_COLLECTION
import com.dk.organizeu.pojo.LessonPojo
import com.dk.organizeu.pojo.LessonPojo.Companion.toLessonPojo
import com.dk.organizeu.pojo.LessonPojo.Companion.toMap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import java.time.LocalTime

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
            timetableDocumentId: String,
            lessonPojo: LessonPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
            ){
            try {
                try {
                    lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
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

        fun updateLesson(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId: String,lessonPojo: LessonPojo,isRenamed:(Boolean) -> Unit){
            lessonDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,lessonPojo.id)
                .update(lessonPojo.toMap())
                .addOnSuccessListener {
                    isRenamed(true)
                }
                .addOnFailureListener {
                    isRenamed(false)
                }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun isLessonDocumentConflict(academicDocumentId: String, weekdayName:String, inputLessonPojo: LessonPojo, conflict: (Boolean) -> Unit)
        {
            try {
                var temp = inputLessonPojo.startTime.split(":")
                val lessonStartTime = LocalTime.of(temp[0].toInt(),temp[1].toInt())
                temp = inputLessonPojo.endTime.split(":")
                val lessonEndTime =  LocalTime.of(temp[0].toInt(),temp[1].toInt())


                val semesterDocuments = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
                for(semesterDoc in semesterDocuments)
                {
                    try {
                        val classDocuments = ClassRepository.getAllClassDocuments(academicDocumentId, semesterDoc.id)
                        for (classDoc in classDocuments)
                        {
                            try {
                                val timetableDocumentId = TimeTableRepository.getTimetableIdByName(academicDocumentId,semesterDoc.id,classDoc.id,weekdayName)
                                val lessonDocuments = getAllLessonDocuments(academicDocumentId, semesterDoc.id, classDoc.id, timetableDocumentId!!)
                                for(lessonDoc in lessonDocuments)
                                {
                                    val lessonPojo = lessonDoc.toLessonPojo()
                                    try {
                                        temp = lessonPojo.startTime.split(":")
                                        val parsedStartTime =  LocalTime.of(temp[0].toInt(),temp[1].toInt())
                                        temp = lessonPojo.endTime.split(":")
                                        val parsedEndTime =  LocalTime.of(temp[0].toInt(),temp[1].toInt())
                                        if (lessonStartTime in parsedStartTime..parsedEndTime) {
                                            if((lessonPojo.facultyName == inputLessonPojo.facultyName) || (lessonPojo.location == inputLessonPojo.location))
                                            {
                                                if(lessonPojo.id != inputLessonPojo.id)
                                                {
                                                    conflict(true)
                                                    return
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
                conflict(false)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }
    }
}