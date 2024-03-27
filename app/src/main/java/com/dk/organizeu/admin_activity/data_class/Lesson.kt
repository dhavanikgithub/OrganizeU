package com.dk.organizeu.admin_activity.data_class

import java.sql.Time

data class Lesson(
    val lessonNumber: Int,
    val type: String,
    val batchName: String?, // Nullable
    val subject: String,
    val faculty: String,
    val roomName: String,
    val roomLocation: String,
    val startTime: Time,
    val endTime: Time
)
