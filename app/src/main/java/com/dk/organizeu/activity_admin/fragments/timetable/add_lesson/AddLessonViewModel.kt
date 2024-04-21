package com.dk.organizeu.activity_admin.fragments.timetable.add_lesson

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.LessonAdapterAdmin
import com.dk.organizeu.pojo.TimetablePojo

class AddLessonViewModel : ViewModel() {
    lateinit var lessonAdapter: LessonAdapterAdmin
    var timetableData: ArrayList<TimetablePojo> = ArrayList()
}