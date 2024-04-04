package com.dk.organizeu.admin_activity.fragments.timetable.add_timetable

import androidx.lifecycle.ViewModel
import com.dk.organizeu.student_activity.adapter.TimetableAdapter
import com.dk.organizeu.student_activity.data_class.TimetableItem

class AddLessonViewModel : ViewModel() {
    lateinit var timetableAdapter: TimetableAdapter
    var timetableData: ArrayList<TimetableItem> = ArrayList()
}