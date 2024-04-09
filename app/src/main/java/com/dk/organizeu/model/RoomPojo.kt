package com.dk.organizeu.model

import android.util.Log
import com.dk.organizeu.admin_activity.data_class.Room
import com.dk.organizeu.model.AcademicPojo.Companion.db
import com.dk.organizeu.firebase.FirebaseConfig.Companion.ROOM_COLLECTION
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class RoomPojo {
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

        fun roomDocumentToRoomObj(document:DocumentSnapshot): Room {
            return Room(
                document.id,
                document.get("location").toString(),
                document.get("type").toString()
            )
        }

        fun roomDocumentToRoomObj(roomDocumentId:String, document:HashMap<String,String>): Room {
            return Room(
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