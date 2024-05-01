package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID


data class LessonPojo(
    val id:String = UUID.randomUUID().toString(),
    var className: String,
    var subjectName: String,
    var subjectCode: String,
    var location: String,
    var startTime: String,
    var endTime: String,
    var duration: String,
    var type: String,
    var facultyName: String,
    var batch: String?,
    val muteRequestCode: Int,
    val unMuteRequestCode: Int,
    val notificationCode: Int,
)
{
    companion object{
        fun DocumentSnapshot.toLessonPojo(): LessonPojo {
            return LessonPojo(
                getString("id") ?: "",
                getString("className") ?: "",
                getString("subjectName") ?: "",
                getString("subjectCode") ?: "",
                getString("location") ?: "",
                getString("startTime") ?: "",
                getString("endTime") ?: "",
                getString("duration") ?: "",
                getString("type") ?: "",
                getString("facultyName") ?: "",
                getString("batch") ?: "",
                getLong("muteRequestCode")!!.toInt(),
                getLong("unMuteRequestCode")!!.toInt(),
                getLong("notificationCode")!!.toInt()
            )
        }
        fun LessonPojo.toMap(): Map<String, Any> {
            return mapOf(
                "id" to id,
                "className" to className,
                "subjectName" to subjectName,
                "subjectCode" to subjectCode,
                "location" to location,
                "startTime" to startTime,
                "endTime" to endTime,
                "duration" to duration,
                "type" to type,
                "facultyName" to facultyName,
                "batch" to (batch ?: ""),
                "muteRequestCode" to muteRequestCode,
                "unMuteRequestCode" to unMuteRequestCode,
                "notificationCode" to notificationCode
            )
        }
    }
}
