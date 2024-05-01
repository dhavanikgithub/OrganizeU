package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

data class RoomPojo(
    val id:String = UUID.randomUUID().toString(),
    var name:String,
    var location:String,
    var type: String)
{
    companion object{
        fun DocumentSnapshot.toRoomPojo(): RoomPojo {
            return RoomPojo(
                id = getString("id") ?: UUID.randomUUID().toString(),
                name = getString("name") ?: "",
                location = getString("location") ?: "",
                type = getString("type") ?: ""
            )
        }

        fun RoomPojo.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            map["id"] = id
            map["name"] = name
            map["location"] = location
            map["type"] = type
            return map
        }
    }
}
