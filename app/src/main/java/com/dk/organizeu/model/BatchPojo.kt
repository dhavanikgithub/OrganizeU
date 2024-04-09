package com.dk.organizeu.model

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.model.ClassPojo.Companion.classDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class BatchPojo {
    companion object{
        fun batchCollectionRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): CollectionReference
        {
            return classDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId)
                .collection(FirebaseConfig.BATCH_COLLECTION)
        }

        fun batchDocumentRef(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String,batchDocumentId:String): DocumentReference
        {
            return batchCollectionRef(academicDocumentId,semesterDocumentId,classDocumentId).document(batchDocumentId)
        }

        suspend fun getAllBatchDocuments(academicDocumentId: String,semesterDocumentId: String,classDocumentId: String): MutableList<DocumentSnapshot> {
            return batchCollectionRef(academicDocumentId, semesterDocumentId, classDocumentId).get().await().documents
        }

        suspend fun insertBatchDocument(
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
            batchDocumentRef(academicDocumentId, semesterDocumentId, classDocumentId, batchDocumentId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    callback(documentSnapshot.exists())
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error checking document existence", exception)
                    callback(false) // Assume document doesn't exist if there's an error
                }
        }
    }
}