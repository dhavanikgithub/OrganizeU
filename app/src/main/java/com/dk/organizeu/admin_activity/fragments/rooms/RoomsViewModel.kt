package com.dk.organizeu.admin_activity.fragments.rooms

import androidx.lifecycle.ViewModel
import com.dk.organizeu.admin_activity.adapter.RoomAdapter
import com.dk.organizeu.admin_activity.pojo.RoomPojo

class RoomsViewModel : ViewModel() {
    lateinit var roomAdapter: RoomAdapter
    val roomPojoList:ArrayList<RoomPojo> = ArrayList()
}