package com.dk.organizeu.activity_admin.fragments.faculty

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.FacultyAdapter

class FacultyViewModel : ViewModel() {
    lateinit var facultyAdapter: FacultyAdapter
    val facultyList: ArrayList<String> = ArrayList()
}