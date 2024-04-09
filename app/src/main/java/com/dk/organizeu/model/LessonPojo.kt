package com.dk.organizeu.model

import com.dk.organizeu.firebase.FirebaseConfig.Companion.WEEKDAY_COLLECTION
import com.dk.organizeu.model.AcademicPojo.Companion.db
import com.dk.organizeu.student_activity.data_class.TimetableItem
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import java.sql.Time

class LessonPojo {
    companion object{
        fun lessonCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String): CollectionReference {
            return TimeTablePojo.timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId).collection(WEEKDAY_COLLECTION)
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
            lessonCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
                .document()
                .set(inputHashMap)
                .addOnSuccessListener {
                    successCallback(inputHashMap)
                }
                .addOnFailureListener {
                    failureCallback(it)
                }
        }

        fun lessonDocumentToLessonObj(document: DocumentSnapshot,counter:Int): TimetableItem {
            return TimetableItem(
                document.get("class_name").toString(),
                document.get("subject_name").toString(),
                document.get("subject_code").toString(),
                document.get("location").toString(),
                document.get("start_time").toString(),
                document.get("end_time").toString(),
                document.get("duration").toString(),
                document.get("type").toString(),
                document.get("faculty").toString(),
                counter
            )
        }

        fun isLessonDocumentExistByField(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,timetableDocumentId:String,fieldName:String,fieldValue:String, callback: (Boolean) -> Unit)
        {
            TimeTablePojo.timetableDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId)
                .collection("weekday")
                .whereEqualTo(fieldName, fieldValue)
                .get()
                .addOnSuccessListener { documents ->
                    callback(documents.isEmpty)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }
    }
}