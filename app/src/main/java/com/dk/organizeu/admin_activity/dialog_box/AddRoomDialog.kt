package com.dk.organizeu.admin_activity.dialog_box

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.listener.RoomAddListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class AddRoomDialog() : AppCompatDialogFragment() {
    private lateinit var roomTypeACTV: AutoCompleteTextView
    private lateinit var roomNameET: TextInputEditText
    private lateinit var roomLocationET: TextInputEditText
    private lateinit var addButton: MaterialButton
    private lateinit var closeButton: MaterialButton

    private lateinit var db: FirebaseFirestore
    private var roomAddListener: RoomAddListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_room_dialog_layout, null)
        db = FirebaseFirestore.getInstance()
        roomAddListener = parentFragment as? RoomAddListener
        roomTypeACTV = view.findViewById(R.id.roomTypeACTV)
        roomLocationET = view.findViewById(R.id.roomLocationET)
        roomNameET = view.findViewById(R.id.roomNameET)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)

        val roomTypeList = arrayOf("CLASS","LAB")
        val roomTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roomTypeList)
        roomTypeACTV.setAdapter(roomTypeAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Room")

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {
            if(isItemSelected(roomTypeACTV) && roomNameET.text.toString()!="" && roomLocationET.text.toString()!="")
            {

                val roomData = hashMapOf(
                    "location" to roomLocationET.text.toString(),
                    "type" to roomTypeACTV.text.toString()

                )
                isRoomDocumentExists(roomNameET.text.toString()) { exists ->
                    if(exists)
                    {
                        closeButton.callOnClick()
                        return@isRoomDocumentExists
                    }
                    addNewRoom(roomNameET.text.toString(),roomData)
                }

            }

        }
        return builder.create()
    }

    private fun addNewRoom(roomDocumentId:String, roomData:HashMap<String,String>)
    {
        db.collection("room")
            .document(roomDocumentId)
            .set(roomData)
            .addOnSuccessListener {
                Log.d("TAG", "Room document added successfully with ID: $roomDocumentId")
                roomAddListener?.onRoomAdded(roomData,roomDocumentId)
                closeButton.callOnClick()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding room document", e)
                closeButton.callOnClick()
            }
    }

    private fun isRoomDocumentExists(roomDocumentId: String, callback: (Boolean) -> Unit) {
        db.collection("room")
            .document(roomDocumentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                callback(documentSnapshot.exists())
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error checking document existence", exception)
                callback(false)
            }
    }


    private fun isItemSelected(autoCompleteTextView: AutoCompleteTextView): Boolean {
        val selectedItem = autoCompleteTextView.text.toString().trim()
        val adapter = autoCompleteTextView.adapter as ArrayAdapter<String>
        for (i in 0 until adapter.count) {
            if (selectedItem == adapter.getItem(i)) {
                return true
            }
        }
        return false
    }

}