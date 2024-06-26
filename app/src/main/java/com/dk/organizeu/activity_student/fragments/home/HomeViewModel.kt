package com.dk.organizeu.activity_student.fragments.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.LessonAdapterStudent
import com.dk.organizeu.pojo.LessonPojo
import kotlin.properties.Delegates

class HomeViewModel : ViewModel() {
    lateinit var lessonAdapter: LessonAdapterStudent
    var timetableData: HashMap<Int,ArrayList<LessonPojo>> = HashMap()
    var dayOfWeek by Delegates.notNull<Int>()
    var currentDayTimeTableData = MutableLiveData(ArrayList<LessonPojo>())
    var selectedWeekDayTab: Int = 0
    var selectedTab: Int = 1


}