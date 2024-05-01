package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.ClassPojo
import com.dk.organizeu.repository.SemesterRepository.Companion.semesterDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class ClassRepository {
    companion object{
        const val TAG = "OrganizeU-ClassRepository"
        fun classCollectionRef(academicDocumentId: String, semesterDocumentId: String): CollectionReference {
            try {
                return semesterDocumentRef(academicDocumentId,semesterDocumentId).collection(
                    FirebaseConfig.CLASS_COLLECTION
                )
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun classDocumentRef(academicDocumentId: String, semesterDocumentId: String, id:String): DocumentReference {
            try {
                return classCollectionRef(academicDocumentId,semesterDocumentId).document(id)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllClassDocuments(academicDocumentId: String,semesterDocumentId: String): MutableList<DocumentSnapshot> {
            try {
                return classCollectionRef(academicDocumentId, semesterDocumentId).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteClassDocument(academicDocumentId: String, semesterDocumentId: String, id: String){
            try {
                BatchRepository.deleteAllBatchDocuments(academicDocumentId, semesterDocumentId, id)
                TimeTableRepository.deleteAllTimetableDocuments(academicDocumentId, semesterDocumentId, id)
                classDocumentRef(academicDocumentId, semesterDocumentId, id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllClassDocuments(academicDocumentId: String,semesterDocumentId: String)
        {
            try {
                getAllClassDocuments(academicDocumentId, semesterDocumentId).map {
                    deleteClassDocument(academicDocumentId,semesterDocumentId,it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertClassDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classPojo: ClassPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                classDocumentRef(academicDocumentId,semesterDocumentId,classPojo.id).set(classPojo)
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

        fun isClassDocumentExistsById(academicDocumentId: String, semesterDocumentId: String, id:String, isExists: (Boolean) -> Unit) {
            try {
                classDocumentRef(academicDocumentId,semesterDocumentId, id)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true) // Assume document doesn't exist if there's an error
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isClassDocumentExistsByName(academicDocumentId: String, semesterDocumentId: String, name:String, isExists: (Boolean) -> Unit) {
            try {
                classCollectionRef(academicDocumentId, semesterDocumentId)
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
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

    }
}