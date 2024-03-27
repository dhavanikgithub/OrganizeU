package com.dk.organizeu.admin_activity.data_class

data class Academic(
    val year: String,
    val type: String,
    val semester: ArrayList<Semester>,
    val faculty: ArrayList<Faculty>,
    val timeTable: ArrayList<TimeTable>
)
