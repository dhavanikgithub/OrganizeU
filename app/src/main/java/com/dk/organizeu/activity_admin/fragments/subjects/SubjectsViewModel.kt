package com.dk.organizeu.activity_admin.fragments.subjects

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.SubjectAdapter
import com.dk.organizeu.pojo.SubjectPojo

class SubjectsViewModel : ViewModel() {
    lateinit var subjectAdapter: SubjectAdapter
    val subjectPojoList:ArrayList<SubjectPojo> = ArrayList()
}