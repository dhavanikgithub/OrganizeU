package com.dk.organizeu.admin_activity.fragments.faculty

import androidx.lifecycle.ViewModel
import com.dk.organizeu.admin_activity.adapter.FacultyAdapter

class FacultyViewModel : ViewModel() {
    lateinit var facultyAdapter: FacultyAdapter
    val facultyList: ArrayList<String> = ArrayList()
}