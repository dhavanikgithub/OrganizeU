package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

data class TimetablePojo(val id:String=UUID.randomUUID().toString(),var name:String)
{
    companion object
    {
        fun DocumentSnapshot.toTimetablePojo(): TimetablePojo {
            return TimetablePojo(
                getString("id") ?: "",
                getString("name") ?: ""
            )
        }
        fun TimetablePojo.toMap(): Map<String, Any> {
            return mapOf(
                "id" to id,
                "name" to name
            )
        }
    }
}
