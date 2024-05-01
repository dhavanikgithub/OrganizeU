package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

data class AcademicPojo(val id: String = UUID.randomUUID().toString(), var year: String, var type: String)
{
    companion object{
        fun DocumentSnapshot.toAcademicPojo(): AcademicPojo {
            return AcademicPojo(
                id = this.id,
                year = this.data?.get("year").toString(),
                type = this.data?.get("type").toString()
            )
        }

        fun AcademicPojo.toMap(): Map<String, Any> {
            return mapOf(
                "id" to this.id,
                "year" to this.year,
                "type" to this.type
            )
        }
    }
}
