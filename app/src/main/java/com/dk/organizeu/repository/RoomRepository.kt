package com.dk.organizeu.repository

import android.util.Log
import com.dk.organizeu.firebase.FirebaseConfig.Companion.ROOM_COLLECTION
import com.dk.organizeu.firebase.key_mapping.RoomCollection
import com.dk.organizeu.pojo.RoomPojo
import com.dk.organizeu.repository.AcademicRepository.Companion.db
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
            try {
                return RoomPojo(
                    document.id,
                    document.get(RoomCollection.LOCATION.displayName).toString(),
                    document.get(RoomCollection.TYPE.displayName).toString()
                )
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun roomDocumentToRoomObj(roomDocumentId:String, document:HashMap<String,String>): RoomPojo {
            try {
                return RoomPojo(
                    roomDocumentId,
                    document[RoomCollection.LOCATION.displayName].toString(),
                    document[RoomCollection.TYPE.displayName].toString()
                )
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isRoomDocumentExists(roomDocumentId: String, callback: (Boolean) -> Unit) {
            try {
                roomDocumentRef(roomDocumentId).get()
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

        suspend fun deleteRoomDocument(roomDocumentId: String){
            try {
                roomDocumentRef(roomDocumentId).delete().await()
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

        fun updateRoomDocument(oldDocumentId: String, newDocumentId: String, roomData:HashMap<String,String>, isRenamed:(Boolean)->Unit) {
            val oldDocRef = roomDocumentRef(oldDocumentId)
            val newDocRef = roomDocumentRef(newDocumentId)

            oldDocRef.get().addOnSuccessListener { oldDocSnapshotTask ->
                val data = oldDocSnapshotTask.data
                newDocRef.set(data!!).addOnSuccessListener {
                    try {
                        newDocRef.update(roomData as Map<String, Any>).addOnSuccessListener {
                            MainScope().launch(Dispatchers.IO)
                            {
                                deleteRoomDocument(oldDocumentId)
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