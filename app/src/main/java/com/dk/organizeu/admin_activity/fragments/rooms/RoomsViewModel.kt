package com.dk.organizeu.admin_activity.fragments.rooms

import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.RoomAdapter
import com.dk.organizeu.pojo.RoomPojo

class RoomsViewModel : ViewModel() {
    lateinit var roomAdapter: RoomAdapter
    val roomPojoList:ArrayList<RoomPojo> = ArrayList()
}