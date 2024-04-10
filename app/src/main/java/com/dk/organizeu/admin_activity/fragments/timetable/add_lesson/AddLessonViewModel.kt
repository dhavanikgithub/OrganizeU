package com.dk.organizeu.admin_activity.fragments.timetable.add_lesson

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.LessonAdapter
import com.dk.organizeu.pojo.TimetablePojo

class AddLessonViewModel : ViewModel() {
    lateinit var lessonAdapter: LessonAdapter
    var timetableData: ArrayList<TimetablePojo> = ArrayList()
}