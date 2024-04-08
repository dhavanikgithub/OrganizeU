package com.dk.organizeu.model

import android.util.Log
import com.dk.organizeu.model.SemesterPojo.Companion.semesterDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class ClassPojo {
    companion object{
        fun classCollectionRef(academicDocumentId: String, semesterDocumentId: String): CollectionReference {
            return semesterDocumentRef(academicDocumentId,semesterDocumentId).collection(
                FirebaseConfig.CLASS_COLLECTION
            )
        }

        fun classDocumentRef(academicDocumentId: String, semesterDocumentId: String, classDocumentId:String): DocumentReference {
            return classCollectionRef(academicDocumentId,semesterDocumentId).document(classDocumentId)
        }

        suspend fun getAllClassDocuments(academicDocumentId: String,semesterDocumentId: String): MutableList<DocumentSnapshot> {
            return classCollectionRef(academicDocumentId, semesterDocumentId).get().await().documents
        }

        suspend fun insertClassDocument(
            academicDocumentId: String,
            semesterDocumentId: String,
            classDocumentId: String,
            inputHashMap:HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                classDocumentRef(academicDocumentId,semesterDocumentId,classDocumentId).set(inputHashMap)
                    .addOnSuccessListener {
                        successCallback(inputHashMap)
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

        fun isClassDocumentExists(academicDocumentId: String,semesterDocumentId: String,classDocumentId:String, callback: (Boolean) -> Unit) {
            classDocumentRef(academicDocumentId,semesterDocumentId, classDocumentId)
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