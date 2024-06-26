package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

data class ClassPojo(
    val id:String = UUID.randomUUID().toString(),
    var name: String
)
{
    companion object{
        fun DocumentSnapshot.toClassPojo(): ClassPojo {
            return ClassPojo(id = this.id, name = this.data?.get("name").toString() ?: "")
        }

        fun ClassPojo.toMap(): Map<String, Any> {
            return mapOf(
                "id" to this.id,
                "name" to this.name
            )
        }
    }
}
