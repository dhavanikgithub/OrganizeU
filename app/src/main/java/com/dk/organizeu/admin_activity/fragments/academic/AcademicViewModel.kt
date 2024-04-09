package com.dk.organizeu.admin_activity.fragments.academic

import androidx.lifecycle.ViewModel
import com.dk.organizeu.admin_activity.pojo.AcademicPojo
import com.dk.organizeu.admin_activity.adapter.AcademicAdapter

class AcademicViewModel : ViewModel() {
    lateinit var academicAdapter: AcademicAdapter
    var academicList : ArrayList<AcademicPojo> = ArrayList()
}