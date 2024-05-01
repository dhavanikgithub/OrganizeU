package com.dk.organizeu.activity_admin.fragments.faculty

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.FacultyAdapter
import com.dk.organizeu.pojo.FacultyPojo

class FacultyViewModel : ViewModel() {
    lateinit var facultyAdapter: FacultyAdapter
    val facultyList: ArrayList<FacultyPojo> = ArrayList()
}