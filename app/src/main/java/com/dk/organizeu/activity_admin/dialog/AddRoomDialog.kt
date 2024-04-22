package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddRoomDialogLayoutBinding
import com.dk.organizeu.firebase.key_mapping.RoomCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlin.collections.HashMap

class AddRoomDialog : AppCompatDialogFragment() {
    private var roomAddListener: AddDocumentListener? = null
    lateinit var binding: AddRoomDialogLayoutBinding

    companion object{
        const val TAG = "OrganizeU-AddRoomDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_room_dialog_layout, null)
        binding = AddRoomDialogLayoutBinding.bind(view)
        var builder:AlertDialog.Builder? = null

        try {
            roomAddListener = parentFragment as? AddDocumentListener
            builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Room")
            binding.apply {
                binding.btnClose.setOnClickListener {
                    dismiss()
                }
                btnAdd.setOnClickListener {
                    try {
                        if(etRoomName.text.toString().trim()!="" && etRoomLocation.text.toString().trim()!="" && (chipLab.isChecked || chipClass.isChecked))
                        {
                            val roomName = etRoomName.text.toString().trim()
                            val roomData = hashMapOf(
                                RoomCollection.LOCATION.displayName to etRoomLocation.text.toString().trim(),
                                RoomCollection.TYPE.displayName to if(chipLab.isChecked) chipLab.text.toString() else chipClass.text.toString()
                            )
                            RoomRepository.isRoomDocumentExists(roomName) { exists ->
                                try {
                                    if(exists)
                                    {
                                        dismiss()
                                        return@isRoomDocumentExists
                                    }
                                    addNewRoom(roomName,roomData)
                                } catch (e: Exception) {
                                    Log.e(TAG,e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }

                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }

                }
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }

        try {
            return builder!!.create()
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }


    private fun addNewRoom(roomDocumentId:String, roomData:HashMap<String,String>)
    {
        RoomRepository.insertRoomDocument(roomDocumentId,roomData,{
            try {
                Log.d("TAG", "Room document added successfully with ID: $roomDocumentId")
                roomAddListener?.onAdded(roomDocumentId,roomData)
                dismiss()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                requireContext().unexpectedErrorMessagePrint(e)
            }
        },{
            Log.w("TAG", "Error adding room document", it)
            requireContext().unexpectedErrorMessagePrint(it)
            dismiss()
        })
    }

}