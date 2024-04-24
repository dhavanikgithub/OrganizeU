package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
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

        fun batchDocumentRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,batchDocumentId:String): DocumentReference
        {
            try {
                return batchCollectionRef(academicDocumentId,semesterDocumentId,classDocumentId).document(batchDocumentId)
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

        suspend fun deleteBatchDocument(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,batchDocumentId: String){
            try {
                batchDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, batchDocumentId).delete().await()
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
            batchDocumentId:String,
            inputHashMap:HashMap<String,String>,
            successCallback:(HashMap<String,String>) -> Unit,
            failureCallback:(Exception) -> Unit
        ){
            try{
                batchDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId,batchDocumentId)
                    .set(inputHashMap)
                    .addOnSuccessListener {
                        successCallback(inputHashMap)
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

        fun isBatchDocumentExists(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,batchDocumentId: String, callback: (Boolean) -> Unit) {
            try {
                batchDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, batchDocumentId)
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
    }
}