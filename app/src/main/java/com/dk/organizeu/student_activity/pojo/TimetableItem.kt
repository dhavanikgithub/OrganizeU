package com.dk.organizeu.student_activity.pojo


data class TimetableItem(
    val className: String,
    val subjectName: String,
    val subjectCode: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val type: String,
    val facultyName: String,
    val lessonNumber: Int,
    val color: String = "#735DA5"
)

