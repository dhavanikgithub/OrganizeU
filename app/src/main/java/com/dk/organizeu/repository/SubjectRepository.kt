package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.pojo.SubjectPojo
import com.dk.organizeu.firebase.FirebaseConfig.Companion.SUBJECT_COLLECTION
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class SubjectRepository {
    companion object{
        fun subjectCollectionRef(): CollectionReference{
            return db.collection(SUBJECT_COLLECTION)
        }

        fun subjectDocumentRef(subjectDocumentId: String): DocumentReference{
            return subjectCollectionRef().document(subjectDocumentId)
        }

        suspend fun getAllSubjectDocuments(): MutableList<DocumentSnapshot> {
            return subjectCollectionRef().get().await().documents
        }

        suspend fun getSubjectDocumentById(subjectDocumentId: String): DocumentSnapshot? {
            return subjectDocumentRef(subjectDocumentId).get().await()
        }

        fun insertSubjectDocument(
            subjectDocumentId: String,
            inputHashMap: HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        )
        {
            try{
                subjectDocumentRef(subjectDocumentId).set(inputHashMap)
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

        fun subjectDocumentToSubjectObj(document: DocumentSnapshot): SubjectPojo {
            return SubjectPojo(document.id,document.get("code").toString(),document.get("type").toString())
        }

        fun subjectDocumentToSubjectObj(subjectDocumentId: String,subjectData: HashMap<String, String>): SubjectPojo {
            return SubjectPojo(subjectDocumentId,subjectData["code"].toString(),subjectData["type"].toString())
        }

        fun isSubjectDocumentExists(subjectDocumentId: String, callback: (Boolean) -> Unit) {
            subjectDocumentRef(subjectDocumentId).get()
                .addOnSuccessListener { documentSnapshot ->
                    callback(documentSnapshot.exists())
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error checking document existence", exception)
                    callback(false)
                }
        }
    }
}