package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddRoomDialogLayoutBinding
import com.dk.organizeu.enum_class.RoomType
import com.dk.organizeu.listener.RoomDocumentListener
import com.dk.organizeu.pojo.RoomPojo
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AddRoomDialog(val roomPojo: RoomPojo?, val position:Int) : AppCompatDialogFragment() {
    private var roomDocumentListener: RoomDocumentListener? = null
    lateinit var binding: AddRoomDialogLayoutBinding

    companion object{
        const val TAG = "OrganizeU-AddRoomDialog"
    }

    /**
     * Creates and configures the dialog for adding a new room.
     * @param savedInstanceState The saved instance state.
     * @return The created dialog.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_room_dialog_layout, null)
        binding = DataBindingUtil.bind(view)!!
        var builder:AlertDialog.Builder? = null

        try {
            // Set the roomAddListener if the parentFragment implements AddDocumentListener
            roomDocumentListener = parentFragment as? RoomDocumentListener
            var title = "Add Room"
            if(roomPojo!=null)
            {
                binding.etRoomName.setText(roomPojo.name)
                binding.etRoomLocation.setText(roomPojo.location)
                if(roomPojo.type.equals(RoomType.LAB.name))
                {
                    binding.chipLab.isChecked=true
                }
                else{
                    binding.chipClass.isChecked=true
                }
                binding.btnAdd.text = "Edit"
                binding.btnAdd.setIconResource(R.drawable.ic_edit)
                title = "Edit Room"
            }
            // Initialize an AlertDialog.Builder with the context of the current fragment
            builder = AlertDialog.Builder(requireContext())
            .setView(view) // Set the custom view for the dialog to the inflated view
            .setTitle(title) // Set the title of the dialog

            binding.apply {
                binding.btnClose.setOnClickListener {
                    dismiss() // Dismiss dialog
                }

                btnAdd.setOnClickListener {
                    try {
                        val roomName = etRoomName.text.toString().trim().replace(Regex("\\s+")," ")
                        val roomLocation = etRoomLocation.text.toString().trim().replace(Regex("\\s+")," ")
                        val roomType = if(chipLab.isChecked) chipLab.text.toString() else chipClass.text.toString()
                        // Check if roomName, roomLocation, and roomType are not empty and at least one type (lab or class) is selected
                        if(roomName!="" && roomLocation!="" && (chipLab.isChecked || chipClass.isChecked))
                        {
                            if(!roomName.containsOnlyAllowedCharacters())
                            {
                                tlRoomName.error = "Room name only contain alphabets, number and - or  _"
                                return@setOnClickListener
                            }
                            tlRoomName.error = null
                            if(!roomLocation.containsOnlyAllowedCharacters())
                            {
                                tlRoomLocation.error = "Room location only contain alphabets, number and - or  _"
                                return@setOnClickListener
                            }
                            tlRoomLocation.error = null
                            if(roomPojo!=null)
                            {
                                roomPojo.name = roomName
                                roomPojo.location = roomLocation
                                roomPojo.type = roomType
                                RoomRepository.isRoomDocumentExistsById(roomPojo.id) { exists ->
                                    try {
                                        // If the room document already exists, dismiss the dialog and return
                                        if(!exists)
                                        {
                                            requireContext().showToast("Room is not exists")
                                            return@isRoomDocumentExistsById
                                        }
                                        RoomRepository.isRoomDocumentConflict(roomPojo){
                                            if(it)
                                            {
                                                requireContext().showToast("Room is name or location duplicate found")
                                                return@isRoomDocumentConflict
                                            }
                                            RoomRepository.updateRoomDocument(roomPojo)
                                            {
                                                if(it)
                                                {
                                                    MainScope().launch(Dispatchers.Main)
                                                    {
                                                        roomDocumentListener!!.onEdited(roomPojo,position)
                                                        dismiss()
                                                    }

                                                }
                                                else{
                                                    requireContext().showToast("Room Data Update Failed")
                                                }
                                            }
                                        }

                                    } catch (e: Exception) {
                                        // Log any unexpected exceptions that occur
                                        Log.e(TAG,e.message.toString())
                                        // Display an unexpected error message to the user
                                        requireContext().unexpectedErrorMessagePrint(e)
                                    }
                                }
                                return@setOnClickListener
                            }
                            val newRoomPojo = RoomPojo(name = roomName, location = roomLocation, type = roomType)

                            // Check if the room document already exists
                            RoomRepository.isRoomDocumentExistsByNameLocation(newRoomPojo) { exists ->
                                try {
                                    // If the room document already exists, dismiss the dialog and return
                                    if(exists)
                                    {
                                        requireContext().showToast("Room is exists")
                                        return@isRoomDocumentExistsByNameLocation
                                    }
                                    // If the room document does not exist, add it to the database
                                    addNewRoom(newRoomPojo)
                                } catch (e: Exception) {
                                    // Log any unexpected exceptions that occur
                                    Log.e(TAG,e.message.toString())
                                    // Display an unexpected error message to the user
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        }
                        else{
                            requireContext().showToast("All fields are required")
                        }
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG,e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }

                }
            }
        } catch (e: Exception) {
            // Log any unexpected exceptions that occur
            Log.e(TAG,e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
        }

        try {
            // Create and return the dialog
            return builder!!.create()
        } catch (e: Exception) {

            Log.e(TAG,e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Adds a new room to the database.
     * @param roomDocumentId The ID of the room document.
     * @param roomData A HashMap containing room data.
     */
    private fun addNewRoom(roomPojo: RoomPojo)
    {
        // Insert the room document into the database by using repository insert method
        RoomRepository.insertRoomDocument(roomPojo,{
            // Success callback
            try {
                Log.d("TAG", "Room document added successfully with ID: ${roomPojo.id}")
                // Notify the listener about the addition of the room document
                roomDocumentListener?.onAdded(roomPojo)

                dismiss() // Dismiss dialog
            } catch (e: Exception) {
                // Log any exceptions that occur
                Log.e(TAG,e.message.toString())
                // Display an unexpected error message to the user
                requireContext().unexpectedErrorMessagePrint(e)
            }
        },{
            // Error callback
            Log.w("TAG", "Error adding room document", it)
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(it)
            dismiss() // Dismiss dialog
        })
    }

}