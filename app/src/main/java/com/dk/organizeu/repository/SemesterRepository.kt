package com.dk.organizeu.repository

import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.repository.AcademicRepository.Companion.academicDocumentRef
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class SemesterRepository {
    companion object{
        fun semesterCollectionRef(academicDocumentId: String): CollectionReference {
            return academicDocumentRef(academicDocumentId).collection(FirebaseConfig.SEMESTER_COLLECTION)
        }

        fun semesterDocumentRef(academicDocumentId: String, semesterDocumentId:String): DocumentReference {
            return semesterCollectionRef(academicDocumentId).document(semesterDocumentId)
        }

        suspend fun getAllSemesterDocuments(academicDocumentId: String): MutableList<DocumentSnapshot> {
            return semesterCollectionRef(academicDocumentId).get().await().documents
        }

        fun insertSemesterDocuments(
            academicDocumentId: String,
            semesterDocumentId:String,
            inputHashMap:HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                semesterDocumentRef(academicDocumentId,semesterDocumentId).set(inputHashMap)
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
    }
}