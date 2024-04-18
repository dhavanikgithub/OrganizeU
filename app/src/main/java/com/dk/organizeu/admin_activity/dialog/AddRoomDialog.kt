package com.dk.organizeu.admin_activity.dialog

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
import com.dk.organizeu.databinding.AddRoomDialogLayoutBinding
import com.dk.organizeu.enum_class.RoomType
import com.dk.organizeu.firebase.key_mapping.RoomCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideKeyboard
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlin.collections.HashMap

class AddRoomDialog : AppCompatDialogFragment() {
    private var roomAddListener: AddDocumentListener? = null
    lateinit var binding: AddRoomDialogLayoutBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_room_dialog_layout, null)
        binding = AddRoomDialogLayoutBinding.bind(view)

        roomAddListener = parentFragment as? AddDocumentListener

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Room")
        binding.apply {
            binding.btnClose.setOnClickListener {
                dismiss()
            }
            btnAdd.setOnClickListener {
                if(etRoomName.text.toString()!="" && etRoomLocation.text.toString()!="" && (chipLab.isChecked || chipClass.isChecked))
                {
                    val roomName = etRoomName.text.toString()
                    val roomData = hashMapOf(
                        RoomCollection.LOCATION.displayName to etRoomLocation.text.toString(),
                        RoomCollection.TYPE.displayName to if(chipLab.isChecked) chipLab.text.toString() else chipClass.text.toString()
                    )
                    RoomRepository.isRoomDocumentExists(roomName) { exists ->
                        if(exists)
                        {
                            dismiss()
                            return@isRoomDocumentExists
                        }
                        addNewRoom(roomName,roomData)
                    }

                }

            }
        }


        return builder.create()
    }


    private fun addNewRoom(roomDocumentId:String, roomData:HashMap<String,String>)
    {
        RoomRepository.insertRoomDocument(roomDocumentId,roomData,{
            Log.d("TAG", "Room document added successfully with ID: $roomDocumentId")
            roomAddListener?.onAdded(roomDocumentId,roomData)
            dismiss()
        },{
            Log.w("TAG", "Error adding room document", it)
            dismiss()
        })
    }

}