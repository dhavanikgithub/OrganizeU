package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.admin_activity.pojo.RoomPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.dk.organizeu.firebase.FirebaseConfig.Companion.ROOM_COLLECTION
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

class RoomRepository {
    companion object{
        fun roomCollectionRef(): CollectionReference{
            return db.collection(ROOM_COLLECTION)
        }

        fun roomDocumentRef(roomDocumentId: String): DocumentReference{
            return roomCollectionRef().document(roomDocumentId)
        }

        suspend fun getAllRoomDocument(): MutableList<DocumentSnapshot> {
            return roomCollectionRef().get().await().documents
        }

        suspend fun getRoomDocumentById(roomDocumentId: String): DocumentSnapshot? {
            return roomDocumentRef(roomDocumentId).get().await()
        }

        suspend fun getRoomDocumentsByField(fieldName:String, fieldValue:String): MutableList<DocumentSnapshot> {
            return roomCollectionRef().whereEqualTo(fieldName,fieldValue).get().await().documents
        }

        fun insertRoomDocument(
            roomDocumentId: String,
            inputHashMap: HashMap<String,String>,
            successCallback: (HashMap<String, String>) -> Unit,
            failureCallback: (Exception) -> Unit
        ){
            try{
                roomDocumentRef(roomDocumentId).set(inputHashMap)
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

        fun roomDocumentToRoomObj(document:DocumentSnapshot): RoomPojo {
            return RoomPojo(
                document.id,
                document.get("location").toString(),
                document.get("type").toString()
            )
        }

        fun roomDocumentToRoomObj(roomDocumentId:String, document:HashMap<String,String>): RoomPojo {
            return RoomPojo(
                roomDocumentId,
                document["location"].toString(),
                document["type"].toString()
            )
        }

        fun isRoomDocumentExists(roomDocumentId: String, callback: (Boolean) -> Unit) {
           roomDocumentRef(roomDocumentId).get()
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