package com.dk.organizeu.activity_admin.fragments.academic

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.AcademicAdapter
import com.dk.organizeu.pojo.AcademicPojo

class AcademicViewModel : ViewModel() {
    lateinit var academicAdapter: AcademicAdapter
    val academicList = ArrayList<AcademicPojo>()

}