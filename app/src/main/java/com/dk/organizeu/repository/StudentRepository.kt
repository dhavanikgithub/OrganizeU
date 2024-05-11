package com.dk.organizeu.repository

import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.StudentPojo
import com.dk.organizeu.pojo.StudentPojo.Companion.toStudentPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class StudentRepository {
    companion object{
        fun studentCollectionRef():CollectionReference{
            return db.collection(FirebaseConfig.STUDENT_COLLECTION)
        }

        fun studentDocumentRef(studentId: String): DocumentReference
        {
            return studentCollectionRef().document(studentId)
        }

        suspend fun getStudentPojoById(studentId: String): StudentPojo{
            return studentDocumentRef(studentId).get().await().toStudentPojo()
        }

        fun isMatchLoginDetails(studentId: String, password:String, isMatch:(Boolean) -> Unit, exception:(Exception) -> Unit){
            studentCollectionRef().whereEqualTo("id",studentId)
                .get()
                .addOnSuccessListener {
                    if(it.isEmpty || it.documents.isEmpty())
                    {
                        isMatch(false)
                        return@addOnSuccessListener
                    }
                    val studentPojo = it.documents[0].toStudentPojo()
                    val temp = password.toSHA256()
                    if(studentPojo.password.equals(temp,true))
                    {
                        isMatch(true)
                    }
                    else{
                        isMatch(false)
                    }
                }
                .addOnFailureListener {
                    exception(it)
                }
        }

        fun insertStudent(
            studentPojo: StudentPojo,
            isInserted: (Boolean) -> Unit
        )
        {
            studentDocumentRef(studentPojo.id).set(studentPojo)
                .addOnSuccessListener {
                    isInserted(true)
                }
                .addOnFailureListener {
                    isInserted(false)
                }
        }

        fun isStudentExistById(studentId: String, isExist:(Boolean) -> Unit)
        {
            studentCollectionRef().whereEqualTo("id",studentId)
                .get()
                .addOnSuccessListener {
                    isExist(!it.isEmpty)
                }
                .addOnFailureListener {
                    isExist(true)
                }
        }

        fun isStudentExistByName(studentName: String,isExist:(Boolean) -> Unit){
            studentCollectionRef().whereEqualTo("name",studentName)
                .get()
                .addOnSuccessListener {
                    isExist(!it.isEmpty)
                }
                .addOnFailureListener {
                    isExist(true)
                }
        }

        fun String.toSHA256(): String {
            val bytes = this.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        fun changePassword(studentId: String,password: String,newPassword:String, isChanged:(Boolean)->Unit, isPasswordMatch:(Boolean)->Unit){
            studentCollectionRef().whereEqualTo("id",studentId).whereEqualTo("password",password.toSHA256())
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty)
                    {
                        isPasswordMatch(false)
                        return@addOnSuccessListener
                    }
                    studentDocumentRef(studentId).update("password",newPassword.toSHA256()).addOnSuccessListener {
                        isChanged(true)
                    }
                    .addOnFailureListener {
                        isChanged(false)
                    }
                }
                .addOnFailureListener {
                    isChanged(false)
                }
        }
    }
}