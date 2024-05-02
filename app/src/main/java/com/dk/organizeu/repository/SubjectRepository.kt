package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig.Companion.SUBJECT_COLLECTION
import com.dk.organizeu.pojo.SubjectPojo
import com.dk.organizeu.pojo.SubjectPojo.Companion.toMap
import com.dk.organizeu.pojo.SubjectPojo.Companion.toSubjectPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class SubjectRepository {
    companion object{
        const val TAG = "OrganizeU-SubjectRepository"
        fun subjectCollectionRef(): CollectionReference{
            try {
                return db.collection(SUBJECT_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun subjectDocumentRef(id: String): DocumentReference{
            try {
                return subjectCollectionRef().document(id)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllSubjectDocuments(): MutableList<DocumentSnapshot> {
            try {
                return subjectCollectionRef().get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getSubjectDocumentById(id: String): DocumentSnapshot? {
            try {
                return subjectDocumentRef(id).get().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertSubjectDocument(
            subjectPojo: SubjectPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
        )
        {
            try{
                subjectDocumentRef(subjectPojo.id).set(subjectPojo)
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

        fun isSubjectDocumentExistsById(id: String, isExists: (Boolean) -> Unit) {
            try {
                subjectDocumentRef(id).get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isSubjectDocumentExistsByName(name: String, isExists: (Boolean) -> Unit) {
            try {
                subjectCollectionRef().whereEqualTo("name",name).get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(!documentSnapshot.isEmpty)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isSubjectDocumentExistsByCode(code: String, isExists: (Boolean) -> Unit) {
            try {
                subjectCollectionRef().whereEqualTo("code",code).get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(!documentSnapshot.isEmpty)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isSubjectDocumentExistsByNameAndCode(subjectPojo: SubjectPojo, isExists: (Boolean) -> Unit) {
            try {
                subjectCollectionRef()
                    .whereEqualTo("name",subjectPojo.name)
                    .get()
                    .addOnSuccessListener { querySnapshot  ->
                        if(querySnapshot.isEmpty)
                        {
                            subjectCollectionRef().whereEqualTo("code",subjectPojo.code).get().addOnSuccessListener {
                                isExists(!it.isEmpty)
                            }
                            .addOnFailureListener {
                                isExists(true)
                            }
                        }
                        else{
                            isExists(true)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isExists(true)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }


        suspend fun deleteSubjectDocument(id: String){
            try {
                subjectDocumentRef(id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllSubjectDocuments(){
            try {
                getAllSubjectDocuments().map {
                    deleteSubjectDocument(it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun updateSubjectDocument(subjectPojo: SubjectPojo, isRenamed:(Boolean)->Unit) {
            val oldDocRef = subjectDocumentRef(subjectPojo.id)
            oldDocRef.update(subjectPojo.toMap()).addOnSuccessListener {
                isRenamed(true)
            }.addOnFailureListener {
                isRenamed(false)
            }

        }

        suspend fun getSubjectPojoByName(name:String):SubjectPojo?
        {
            return try {
                subjectCollectionRef().whereEqualTo("name",name).get().await().documents[0].toSubjectPojo()
            } catch (e: Exception) {
                null
            }
        }
    }
}