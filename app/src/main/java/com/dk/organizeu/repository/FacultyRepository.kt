package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.dk.organizeu.firebase.FirebaseConfig.Companion.FACULTY_COLLECTION
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class FacultyRepository {
    companion object{
        const val TAG = "OrganizeU-FacultyRepository"
        fun facultyCollectionRef(): CollectionReference {
            try {
                return db.collection(FACULTY_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun facultyDocumentRef(facultyDocumentId:String): DocumentReference{
            try {
                return facultyCollectionRef().document(facultyDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllFacultyDocuments(): MutableList<DocumentSnapshot> {
            try {
                return facultyCollectionRef().get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getFacultyDocumentById(facultyDocumentId:String): DocumentSnapshot? {
            try {
                return facultyCollectionRef().document(facultyDocumentId).get().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
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