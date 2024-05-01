package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

data class FacultyPojo(val id:String = UUID.randomUUID().toString(), var name:String)
{
    companion object{
        fun DocumentSnapshot.toFacultyPojo(): FacultyPojo {
            return FacultyPojo(
                id = id,
                name = getString("name") ?: ""
            )
        }

        fun FacultyPojo.toMap(): Map<String, Any> {
            val map = hashMapOf<String, Any>()
            map["id"] = id
            map["name"] = name
            return map
        }
    }


}
