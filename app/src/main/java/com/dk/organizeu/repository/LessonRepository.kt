package com.dk.organizeu.repository

import com.dk.organizeu.firebase.FirebaseConfig.Companion.WEEKDAY_COLLECTION
import com.dk.organizeu.firebase.key_mapping.TimeTableCollection
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.utils.UtilFunction.Companion.timeFormat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class LessonRepository {
    companion object{
        fun lessonCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): CollectionReference {
            return TimeTableRepository.timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).collection(WEEKDAY_COLLECTION)
        }

        suspend fun getAllLessonDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): MutableList<DocumentSnapshot> {
            return lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).get().await().documents
        }

        suspend fun getAllLessonDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String,orderBy:String): MutableList<DocumentSnapshot> {
            return lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).orderBy(orderBy).get().await().documents
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
            val myHashMap = hashMapOf(
                TimeTableCollection.WEEKDAY.displayName to timetableDocumentId
            )
            TimeTableRepository.insertTimeTableDocument(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId, myHashMap,
                {
                    lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
                        .document()
                        .set(inputHashMap)
                        .addOnSuccessListener {
                            successCallback(inputHashMap)
                        }
                        .addOnFailureListener {
                            failureCallback(it)
                        }
                }, {

                })

        }

        fun lessonDocumentToLessonObj(document: DocumentSnapshot,counter:Int): TimetablePojo {
            return TimetablePojo(
                document.get(WeekdayCollection.CLASS_NAME.displayName).toString(),
                document.get(WeekdayCollection.SUBJECT_NAME.displayName).toString(),
                document.get(WeekdayCollection.SUBJECT_CODE.displayName).toString(),
                document.get(WeekdayCollection.LOCATION.displayName).toString(),
                document.get(WeekdayCollection.START_TIME.displayName).toString(),
                document.get(WeekdayCollection.END_TIME.displayName).toString(),
                document.get(WeekdayCollection.DURATION.displayName).toString(),
                document.get(WeekdayCollection.TYPE.displayName).toString(),
                document.get(WeekdayCollection.FACULTY_NAME.displayName).toString(),
                counter
            )
        }

        suspend fun isLessonDocumentConflict(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, timetableDocumentId:String, startTime:String, endTime:String, facultyName:String, callback: (Boolean) -> Unit)
        {
            val lessonStartTime = timeFormat.parse(startTime)
            val lessonEndTime = timeFormat.parse(endTime)
            val semesterDocuments = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
            for(semesterDoc in semesterDocuments)
            {
                val classDocuments = ClassRepository.getAllClassDocuments(academicDocumentId, semesterDoc.id)
                for (classDoc in classDocuments)
                {
                    val lessonDocuments = getAllLessonDocuments(academicDocumentId, semesterDoc.id, classDoc.id, timetableDocumentId)
                    for(lessonDoc in lessonDocuments)
                    {
                        val parsedStartTime = timeFormat.parse(lessonDoc.get(WeekdayCollection.START_TIME.displayName).toString())
                        val parsedEndTime = timeFormat.parse(lessonDoc.get(WeekdayCollection.END_TIME.displayName).toString())
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
                                    }
                                    else{
                                        callback(true)
                                        return
                                    }
                                }
                            }
                        }
                    }
                }
            }
            callback(false)
        }
    }
}