package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.pojo.AcademicPojo.Companion.toAcademicPojo
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AcademicRepository {
    companion object{
        val db = FirebaseFirestore.getInstance()
        const val TAG = "OrganizeU-AcademicRepository"

        fun academicCollectionRef(): CollectionReference {
            try {
                return db.collection(FirebaseConfig.ACADEMIC_COLLECTION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun academicDocumentRef(academicDocumentId:String): DocumentReference {
            try {
                return academicCollectionRef().document(academicDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllAcademicDocuments(): MutableList<DocumentSnapshot> {
            try {
                return academicCollectionRef().get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAcademicPojoByYearAndType(year:String, type:String): AcademicPojo? {
            return try {
                academicCollectionRef()
                    .whereEqualTo("year", year)
                    .whereEqualTo("type", type)
                    .get().await().documents[0].toAcademicPojo()
            } catch (e: Exception) {
                null
            }
        }

        suspend fun getAcademicIdByYearAndType(year:String, type:String): String? {
            return try {
                academicCollectionRef()
                    .whereEqualTo("year", year)
                    .whereEqualTo("type", type)
                    .get().await().documents[0].id
            } catch (e: Exception) {
                null
            }
        }



        suspend fun isAcademicDocumentExists(id: String): Boolean {
            try {
                return suspendCoroutine { continuation ->
                    academicDocumentRef(id).get()
                        .addOnSuccessListener { documentSnapshot ->
                            continuation.resume(documentSnapshot.exists())
                        }
                        .addOnFailureListener { exception ->
                            Log.w("TAG", "Error checking academic document existence", exception)
                            continuation.resume(false)
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun insertAcademicDocuments(
            academicPojo: AcademicPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                academicDocumentRef(academicPojo.id).set(academicPojo)
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

        fun isAcademicDocumentExistsById(id: String, isExists: (Boolean) -> Unit) {
            try {
                academicDocumentRef(id).get()
                    .addOnSuccessListener { documentSnapshot ->
                        isExists(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking academic document existence", exception)
                        isExists(false)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isAcademicDocumentExistsByYearAndType(academicPojo: AcademicPojo, isExists: (Boolean) -> Unit) {
            try {
                academicCollectionRef()
                    .whereEqualTo("year",academicPojo.year)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if(documentSnapshot.isEmpty)
                        {
                            isExists(false)
                        }
                        else{
                            for (doc in documentSnapshot.documents)
                            {
                                val academicData = doc.toAcademicPojo()
                                if(academicData.type == academicPojo.type)
                                {
                                    isExists(true)
                                    return@addOnSuccessListener
                                }
                            }
                            isExists(false)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking academic document existence", exception)
                        isExists(false)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAcademicDocument(id: String){
            try {
                SemesterRepository.deleteAllSemesterDocuments(id)
                academicDocumentRef(id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllAcademicDocuments(){
            try {
                getAllAcademicDocuments().map {
                    deleteAcademicDocument(it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

    }
}