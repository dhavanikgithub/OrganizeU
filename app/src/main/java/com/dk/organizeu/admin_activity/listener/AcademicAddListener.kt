package com.dk.organizeu.admin_activity.listener

interface AcademicAddListener {
    fun onAcademicAdded(academicDocumentId: String)
}

interface RoomAddListener {
    fun onRoomAdded(roomData: HashMap<String,String>, roomDocumentId:String)
}