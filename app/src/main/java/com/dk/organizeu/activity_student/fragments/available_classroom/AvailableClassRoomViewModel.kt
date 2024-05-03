package com.dk.organizeu.activity_student.fragments.available_classroom

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.AvailableClassRoomAdapter
import com.dk.organizeu.pojo.AvailableClassRoomPojo
import com.dk.organizeu.pojo.LessonPojo
import com.dk.organizeu.pojo.RoomPojo
import kotlin.properties.Delegates

class AvailableClassRoomViewModel : ViewModel() {
    val availableClassRoomPojos = ArrayList<AvailableClassRoomPojo>()
    var availableClassRoomAdapter:AvailableClassRoomAdapter? =null
    val lessonData = ArrayList<LessonPojo>()
    val roomData = ArrayList<RoomPojo>()
    var timetableData: HashMap<Int,ArrayList<LessonPojo>> = HashMap()
    var dayOfWeek by Delegates.notNull<Int>()
}