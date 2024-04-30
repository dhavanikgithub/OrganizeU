package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig.Companion.SUBJECT_COLLECTION
import com.dk.organizeu.firebase.key_mapping.SubjectCollection
import com.dk.organizeu.pojo.SubjectPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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

        fun subjectDocumentRef(subjectDocumentId: String): DocumentReference{
            try {
                return subjectCollectionRef().document(subjectDocumentId)
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

        suspend fun getSubjectDocumentById(subjectDocumentId: String): DocumentSnapshot? {
            try {
                return subjectDocumentRef(subjectDocumentId).get().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
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
            try {
                return SubjectPojo(document.id,document.get(SubjectCollection.CODE.displayName).toString(),document.get(SubjectCollection.TYPE.displayName).toString())
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun subjectDocumentToSubjectObj(subjectDocumentId: String,subjectData: HashMap<String, String>): SubjectPojo {
            try {
                return SubjectPojo(subjectDocumentId,subjectData[SubjectCollection.CODE.displayName].toString(),subjectData[SubjectCollection.TYPE.displayName].toString())
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isSubjectDocumentExists(subjectDocumentId: String, code:String, callback: (Boolean) -> Unit) {
            try {
                subjectDocumentRef(subjectDocumentId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if(!documentSnapshot.exists())
                        {
                            subjectCollectionRef()
                                .get().addOnSuccessListener {documents ->
                                for(document in documents.documents)
                                {
                                    val subjectCode = document.getString(SubjectCollection.CODE.displayName)
                                    if (subjectCode==code)
                                    {
                                        callback(true)
                                        return@addOnSuccessListener
                                    }
                                }
                                callback(false)
                                return@addOnSuccessListener
                            }
                        }
                        else{
                            callback(true)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        callback(false)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isSubjectDocumentExists(subjectDocumentId: String, callback: (Boolean) -> Unit) {
            try {
                subjectDocumentRef(subjectDocumentId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        callback(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        callback(false)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteSubjectDocument(subjectDocumentId: String){
            try {
                subjectDocumentRef(subjectDocumentId).delete().await()
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

        fun updateSubjectDocument(oldDocumentId: String, newDocumentId: String, inputData:HashMap<String,String>, isRenamed:(Boolean)->Unit) {
            val oldDocRef = subjectDocumentRef(oldDocumentId)
            if(oldDocumentId==newDocumentId)
            {
                oldDocRef.update(inputData as Map<String, Any>).addOnSuccessListener {
                    isRenamed(true)
                }.addOnFailureListener {
                    isRenamed(false)
                }
            }
            else{
                val newDocRef = subjectDocumentRef(newDocumentId)

                oldDocRef.get().addOnSuccessListener { oldDocSnapshotTask ->
                    val data = oldDocSnapshotTask.data
                    newDocRef.set(data!!).addOnSuccessListener {
                        try {
                            newDocRef.update(inputData as Map<String, Any>).addOnSuccessListener {
                                MainScope().launch(Dispatchers.IO)
                                {
                                    deleteSubjectDocument(oldDocumentId)
                                    isRenamed(true)
                                }
                            }.addOnFailureListener {
                                isRenamed(false)
                            }
                        } catch (e: Exception) {
                            isRenamed(false)
                        }
                    }.addOnFailureListener {
                        isRenamed(false)
                    }
                }.addOnFailureListener {
                    isRenamed(false)
                }
            }

        }
    }
}