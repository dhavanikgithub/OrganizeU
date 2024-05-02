package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.SemesterPojo
import com.dk.organizeu.pojo.SemesterPojo.Companion.toSemesterPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.academicDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class SemesterRepository {
    companion object{
        const val TAG = "OrganizeU-SemesterRepository"
        fun semesterCollectionRef(academicDocumentId: String): CollectionReference {
            try {
                return academicDocumentRef(academicDocumentId).collection(FirebaseConfig.SEMESTER_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun semesterDocumentRef(academicDocumentId: String, id:String): DocumentReference {
            try {
                return semesterCollectionRef(academicDocumentId).document(id)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllSemesterDocuments(academicDocumentId: String): MutableList<DocumentSnapshot> {
            try {
                return semesterCollectionRef(academicDocumentId).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteSemesterDocument(academicDocumentId: String, id: String)
        {
            try {
                ClassRepository.deleteAllClassDocuments(academicDocumentId, id)
                semesterDocumentRef(academicDocumentId, id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllSemesterDocuments(academicDocumentId: String)
        {
            try {
                getAllSemesterDocuments(academicDocumentId).map {
                    deleteSemesterDocument(academicDocumentId,it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertSemesterDocuments(
            academicDocumentId: String,
            semesterPojo: SemesterPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                semesterDocumentRef(academicDocumentId,semesterPojo.id).set(semesterPojo)
                    .addOnSuccessListener {
                        successCallback(true)
                    }
                    .addOnFailureListener {
                        failureCallback(it)
                    }
            }
            catch (e:java.lang.Exception)
            {
                failureCallback(e)
            }
        }

        suspend fun getSemesterPojoByName(academicDocumentId: String,name:String): SemesterPojo? {
            return try {
                semesterCollectionRef(academicDocumentId)
                    .whereEqualTo("name", name)
                    .get().await().documents[0].toSemesterPojo()
            } catch (e: Exception) {
                null
            }
        }

        suspend fun getSemesterIdByName(academicDocumentId: String,name:String): String? {
            return try {
                getSemesterPojoByName(academicDocumentId, name)!!.id
            } catch (e: Exception) {
                null
            }
        }

        fun isSemesterDocumentExistsById(academicDocumentId: String, id: String, isExists: (Boolean) -> Unit) {
            try {
                semesterDocumentRef(academicDocumentId, id)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true) // Assume document doesn't exist if there's an error
                    }
            } catch (e: Exception) {
                Log.e(ClassRepository.TAG,e.message.toString())
                throw e
            }
        }


        fun isSemesterDocumentExistsByName(academicDocumentId: String, name: String, isExists: (Boolean) -> Unit) {
            try {
                semesterCollectionRef(academicDocumentId)
                    .whereEqualTo("name",name)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(!documentSnapshot.isEmpty)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true) // Assume document doesn't exist if there's an error
                    }
            } catch (e: Exception) {
                Log.e(ClassRepository.TAG,e.message.toString())
                throw e
            }
        }
    }
}