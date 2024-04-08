package com.dk.organizeu.model

import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

class FirebaseConfig {
    companion object{
        val ACADEMIC_COLLECTION = "academic"
        val SEMESTER_COLLECTION = "semester"
        val CLASS_COLLECTION = "class"
        val TIMETABLE_COLLECTION = "timetable"
        val BATCH_COLLECTION = "batch"
        val WEEKDAY_COLLECTION = "weekday"
        val FACULTY_COLLECTION = "faculty"
        val ROOM_COLLECTION = "room"
        val SUBJECT_COLLECTION = "subject"

    }
}