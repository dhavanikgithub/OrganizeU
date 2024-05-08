package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot

data class StudentPojo(
    val id:String,
    var name:String,
    var academicYear:String,
    var academicType: String,
    var semester:String,
    var className:String,
    var batchName: String,
    var password:String
)
{
    companion object{
        fun DocumentSnapshot.toStudentPojo(): StudentPojo {
            val id = id
            val name = getString("name") ?: ""
            val academicYear = getString("academicYear") ?: ""
            val academicType = getString("academicType") ?: ""
            val semester = getString("semester") ?: ""
            val className = getString("className") ?: ""
            val batchName = getString("batchName") ?: ""
            val password = getString("password") ?: ""
            return StudentPojo(id, name, academicYear, academicType, semester, className, batchName, password)
        }

        fun StudentPojo.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            map["id"] = id
            map["name"] = name
            map["academicYear"] = academicYear
            map["academicType"] = academicType
            map["semester"] = semester
            map["className"] = className
            map["batchName"] = batchName
            map["password"] = password
            return map
        }
    }
}