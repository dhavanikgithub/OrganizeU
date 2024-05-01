package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

data class SubjectPojo(
    val id:String = UUID.randomUUID().toString(),
    var name:String,
    var code:String,
    var type:String)
{
    companion object{
        fun DocumentSnapshot.toSubjectPojo(): SubjectPojo {
            return SubjectPojo(
                id = id,
                name = getString("name") ?: "",
                code = getString("code") ?: "",
                type = getString("type") ?: ""
            )
        }

        fun SubjectPojo.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            map["id"] = id
            map["name"] = name
            map["code"] = code
            map["type"] = type
            return map
        }
    }
}
