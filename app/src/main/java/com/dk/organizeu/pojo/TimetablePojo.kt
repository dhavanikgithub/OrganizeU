package com.dk.organizeu.pojo


data class TimetablePojo(
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
    val muteRequestCode: Int,
    val unmuteRequestCode: Int,
    val notificationCode: Int,
    val color: String = "#735DA5",
)

