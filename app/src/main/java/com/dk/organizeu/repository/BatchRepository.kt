package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.BatchPojo
import com.dk.organizeu.repository.ClassRepository.Companion.classDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class BatchRepository {
    companion object{
        const val TAG = "OrganizeU-BatchRepository"
        fun batchCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): CollectionReference
        {
            try {
                return classDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId)
                    .collection(FirebaseConfig.BATCH_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun batchDocumentRef(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, id:String): DocumentReference
        {
            try {
                return batchCollectionRef(academicDocumentId,semesterDocumentId,classDocumentId).document(id)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllBatchDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): MutableList<DocumentSnapshot> {
            try {
                return batchCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteBatchDocument(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, id: String){
            try {
                batchDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }
        suspend fun deleteAllBatchDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String){
            try {
                getAllBatchDocuments(academicDocumentId, semesterDocumentId, classDocumentId).map {
                    deleteBatchDocument(academicDocumentId,semesterDocumentId,classDocumentId,it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertBatchDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            batchPojo: BatchPojo,
            successCallback:(Boolean) -> Unit,
            failureCallback:(Exception) -> Unit
        ){
            try{
                batchDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId,batchPojo.id)
                    .set(batchPojo)
                    .addOnSuccessListener {
                        successCallback(true)
                    }
                    .addOnFailureListener {
                        failureCallback(it)
                    }

            }
            catch (e: Exception)
            {
                failureCallback(e)
            }
        }

        fun isBatchDocumentExistsById(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, id: String, callback: (Boolean) -> Unit) {
            try {
                batchDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, id)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        callback(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        callback(false) // Assume document doesn't exist if there's an error
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isBatchDocumentExistsByName(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String, name: String, callback: (Boolean) -> Unit) {
            try {
                batchCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId)
                    .whereEqualTo("name",name)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        callback(!documentSnapshot.isEmpty())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        callback(false) // Assume document doesn't exist if there's an error
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }
    }
}