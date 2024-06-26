package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig.Companion.ROOM_COLLECTION
import com.dk.organizeu.pojo.RoomPojo
import com.dk.organizeu.pojo.RoomPojo.Companion.toMap
import com.dk.organizeu.pojo.RoomPojo.Companion.toRoomPojo
import com.dk.organizeu.pojo.SubjectPojo.Companion.toSubjectPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class RoomRepository {
    companion object{
        const val TAG = "OrganizeU-RoomRepository"
        fun roomCollectionRef(): CollectionReference{
            try {
                return db.collection(ROOM_COLLECTION)
            } catch (e: Exception)
            {
                Log.e(TAG,e.message.toString())
                throw e
            }

        }

        fun roomDocumentRef(roomDocumentId: String): DocumentReference{
            try {
                return roomCollectionRef().document(roomDocumentId)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getAllRoomDocument(): MutableList<DocumentSnapshot> {
            try {
                return roomCollectionRef().get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getRoomDocumentById(roomDocumentId: String): DocumentSnapshot? {
            try {
                return roomDocumentRef(roomDocumentId).get().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun getRoomDocumentsByField(fieldName:String, fieldValue:String): MutableList<DocumentSnapshot> {
            try {
                return roomCollectionRef().whereEqualTo(fieldName,fieldValue).get().await().documents
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }


        fun insertRoomDocument(
            roomPojo: RoomPojo,
            successCallback: (Boolean) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                roomDocumentRef(roomPojo.id).set(roomPojo)
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

        fun isRoomDocumentExistsById(id:String, callback: (Boolean) -> Unit) {
            try {
                roomDocumentRef(id).get()
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
        fun isRoomDocumentConflict(roomPojo: RoomPojo, isConflict: (Boolean) -> Unit) {
            try {
                roomCollectionRef()
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        var isConflict = false
                        if (!documentSnapshot.isEmpty) {
                            for(document in documentSnapshot.documents)
                            {
                                val temp = document.toRoomPojo()
                                if(temp.location == roomPojo.location && temp.name == roomPojo.name)
                                {
                                    if(temp.id != roomPojo.id)
                                    {
                                        isConflict = true
                                        break
                                    }
                                }
                            }
                        }
                        isConflict(isConflict)
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        isConflict(true)
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isRoomDocumentExistsByNameLocation(roomPojo: RoomPojo, isExists: (Boolean) -> Unit) {
            try {
                roomCollectionRef().whereEqualTo("name",roomPojo.name).whereEqualTo("location",roomPojo.location).get()
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

        suspend fun deleteRoomDocument(id: String){
            try {
                roomDocumentRef(id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        suspend fun deleteAllRoomDocuments(){
            try {
                getAllRoomDocument().map {
                    deleteRoomDocument(it.id)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun updateRoomDocument(roomPojo: RoomPojo, isRenamed:(Boolean)->Unit) {
            val oldDocRef = roomDocumentRef(roomPojo.id)
            oldDocRef.update(roomPojo.toMap()).addOnSuccessListener {
                isRenamed(true)
            }
            .addOnFailureListener {
                isRenamed(false)
            }
        }

        suspend fun getRoomPojoByNameLocation(name:String,location:String):RoomPojo?
        {
            return try {
                roomCollectionRef().whereEqualTo("name",name).whereEqualTo("location",location).get().await().documents[0].toRoomPojo()
            }
            catch (e: Exception)
            {
                null
            }
        }

    }
}