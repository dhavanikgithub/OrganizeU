package com.dk.organizeu.pojo

import com.google.firebase.firestore.DocumentSnapshot

data class AdminPojo(val id:String, var name:String, var password:String)
{
    companion object{
        fun DocumentSnapshot.toAdminPojo(): AdminPojo {
            val id = id
            val name = getString("name") ?: ""
            val password = getString("password") ?: ""
            return AdminPojo(id, name, password)
        }
    }
}
