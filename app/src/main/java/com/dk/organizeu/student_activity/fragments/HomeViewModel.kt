package com.dk.organizeu.student_activity.fragments

import androidx.lifecycle.ViewModel
import com.dk.organizeu.student_activity.adapter.TimetableAdapter
import com.dk.organizeu.student_activity.data_class.TimetableItem
import kotlin.properties.Delegates

class HomeViewModel : ViewModel() {
    lateinit var timetableAdapter: TimetableAdapter
    lateinit var timetableData: Map<Int,ArrayList<TimetableItem>>
    var dayOfWeek by Delegates.notNull<Int>()
    var currentDayTimeTableData = ArrayList<TimetableItem>()
    var selectedWeekDayTab: Int = 0
    var selectedTab: Int = 1
}