package com.dk.organizeu.admin_activity.fragments.rooms

import androidx.lifecycle.ViewModel
import com.dk.organizeu.admin_activity.adapter.RoomAdapter
import com.dk.organizeu.admin_activity.data_class.Room

class RoomsViewModel : ViewModel() {
    lateinit var roomAdapter: RoomAdapter
    val roomList:ArrayList<Room> = ArrayList()
}