package com.dk.organizeu.admin_activity.fragments.academic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.adapter.AcademicAdapter

class AcademicViewModel : ViewModel() {
    lateinit var academicAdapter: AcademicAdapter
    val academicList =  MutableLiveData(ArrayList<AcademicPojo>())

}