package com.dk.organizeu.repository

import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.dk.organizeu.firebase.FirebaseConfig.Companion.FACULTY_COLLECTION
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class FacultyRepository {
    companion object{
        fun facultyCollectionRef(): CollectionReference {
            return db.collection(FACULTY_COLLECTION)
        }

        fun facultyDocumentRef(facultyDocumentId:String): DocumentReference{
            return facultyCollectionRef().document(facultyDocumentId)
        }

        suspend fun getAllFacultyDocuments(): MutableList<DocumentSnapshot> {
            return facultyCollectionRef().get().await().documents
        }

        fun insertFacultyDocument(
            facultyDocumentId: String,
            inputHashMap:HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                facultyDocumentRef(facultyDocumentId).set(inputHashMap)
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