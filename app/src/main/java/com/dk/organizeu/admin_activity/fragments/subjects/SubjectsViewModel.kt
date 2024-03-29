package com.dk.organizeu.admin_activity.fragments.subjects

import androidx.lifecycle.ViewModel
import com.dk.organizeu.admin_activity.adapter.RoomAdapter
import com.dk.organizeu.admin_activity.adapter.SubjectAdapter
import com.dk.organizeu.admin_activity.data_class.Room
import com.dk.organizeu.admin_activity.data_class.Subject

class SubjectsViewModel : ViewModel() {
    lateinit var subjectAdapter: SubjectAdapter
    val subjectList:ArrayList<Subject> = ArrayList()
}