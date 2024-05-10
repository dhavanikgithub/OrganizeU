package com.dk.organizeu.repository

import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.AdminPojo
import com.dk.organizeu.pojo.AdminPojo.Companion.toAdminPojo
import com.dk.organizeu.repository.StudentRepository.Companion.toSHA256
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

class AdminRepository {

    companion object{
        fun adminCollectionRef(): CollectionReference {
            return AcademicRepository.db.collection(FirebaseConfig.ADMIN_COLLECTION)
        }

        fun adminDocumentRef(adminId:String): DocumentReference{
            return adminCollectionRef().document(adminId)
        }

        fun isMatchAdminCrediantails(adminId: String, password:String, isMatch:(Boolean) -> Unit)
        {
            adminDocumentRef(adminId).get()
                .addOnSuccessListener {
                    if (it==null)
                    {
                        isMatch(false)
                        return@addOnSuccessListener
                    }
                    val adminPojo = it.toAdminPojo()
                    val cipherTextPassword = password.toSHA256()
                    isMatch(adminPojo.password == cipherTextPassword)
                }
                .addOnFailureListener {
                    isMatch(false)
                }
        }

        fun getAdminPojoById(adminId: String, response:(AdminPojo?)->Unit)
        {
            try {
                adminDocumentRef(adminId).get().addOnSuccessListener {
                    response(it.toAdminPojo())
                }
                .addOnFailureListener {
                    response(null)
                }
            }
            catch (e:Exception)
            {
                response(null)
            }
        }
    }
}