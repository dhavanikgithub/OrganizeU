package com.dk.organizeu.student_activity.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.LessonAdapter
import com.dk.organizeu.pojo.TimetablePojo
import kotlin.properties.Delegates

class HomeViewModel : ViewModel() {
    lateinit var lessonAdapter: LessonAdapter
    var timetableData: HashMap<Int,ArrayList<TimetablePojo>> = HashMap()
    var dayOfWeek by Delegates.notNull<Int>()
    var currentDayTimeTableData = MutableLiveData(ArrayList<TimetablePojo>())
    var selectedWeekDayTab: Int = 0
    var selectedTab: Int = 1
}