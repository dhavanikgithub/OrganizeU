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
import com.dk.organizeu.admin_activity.enum_class.RoomType
import com.dk.organizeu.admin_activity.listener.RoomAddListener
import com.dk.organizeu.model.RoomPojo
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideKeyboard
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
    private var roomAddListener: RoomAddListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_room_dialog_layout, null)

        roomAddListener = parentFragment as? RoomAddListener
        roomTypeACTV = view.findViewById(R.id.roomTypeACTV)
        roomLocationET = view.findViewById(R.id.roomLocationET)
        roomNameET = view.findViewById(R.id.roomNameET)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)

        val roomTypeList = arrayOf(RoomType.CLASS.name,RoomType.LAB.name)
        val roomTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roomTypeList)
        roomTypeACTV.setAdapter(roomTypeAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Room")

        roomTypeACTV.setOnClickListener {
            requireContext().hideKeyboard(view)
        }

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {
            if(UtilFunction.isItemSelected(roomTypeACTV) && roomNameET.text.toString()!="" && roomLocationET.text.toString()!="")
            {
                val roomName = roomNameET.text.toString()
                val roomData = hashMapOf(
                    "location" to roomLocationET.text.toString(),
                    "type" to roomTypeACTV.text.toString()

                )
                RoomPojo.isRoomDocumentExists(roomName) { exists ->
                    if(exists)
                    {
                        closeButton.callOnClick()
                        return@isRoomDocumentExists
                    }
                    addNewRoom(roomName,roomData)
                }

            }

        }
        return builder.create()
    }

    private fun addNewRoom(roomDocumentId:String, roomData:HashMap<String,String>)
    {
        RoomPojo.insertRoomDocument(roomDocumentId,roomData,{
            Log.d("TAG", "Room document added successfully with ID: $roomDocumentId")
            roomAddListener?.onRoomAdded(roomData,roomDocumentId)
            closeButton.callOnClick()
        },{
            Log.w("TAG", "Error adding room document", it)
            closeButton.callOnClick()
        })
    }

}