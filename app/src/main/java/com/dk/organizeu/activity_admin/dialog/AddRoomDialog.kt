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
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class AddRoomDialog : AppCompatDialogFragment() {
    private var roomAddListener: AddDocumentListener? = null
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
        binding = AddRoomDialogLayoutBinding.bind(view)
        var builder:AlertDialog.Builder? = null

        try {
            // Set the roomAddListener if the parentFragment implements AddDocumentListener
            roomAddListener = parentFragment as? AddDocumentListener

            // Initialize an AlertDialog.Builder with the context of the current fragment
            builder = AlertDialog.Builder(requireContext())
            .setView(view) // Set the custom view for the dialog to the inflated view
            .setTitle("Add Room") // Set the title of the dialog

            binding.apply {
                binding.btnClose.setOnClickListener {
                    dismiss() // Dismiss dialog
                }

                btnAdd.setOnClickListener {
                    try {
                        val roomName = etRoomName.text.toString().trim()
                        val roomLocation = etRoomLocation.text.toString().trim()
                        // Check if roomName, roomLocation, and roomType are not empty and at least one type (lab or class) is selected
                        if(roomName!="" && roomLocation!="" && (chipLab.isChecked || chipClass.isChecked))
                        {

                            val roomData = hashMapOf(
                                RoomCollection.LOCATION.displayName to roomLocation,
                                RoomCollection.TYPE.displayName to if(chipLab.isChecked) chipLab.text.toString() else chipClass.text.toString()
                            )

                            // Check if the room document already exists
                            RoomRepository.isRoomDocumentExists(roomName) { exists ->
                                try {
                                    // If the room document already exists, dismiss the dialog and return
                                    if(exists)
                                    {
                                        requireContext().showToast("Room is exists")
                                        return@isRoomDocumentExists
                                    }
                                    if(!roomName.containsOnlyAllowedCharacters())
                                    {
                                        tlRoomName.error = "Room name only contain alphabets, number and - or  _"
                                        return@isRoomDocumentExists
                                    }
                                    tlRoomName.error = null
                                    if(!roomLocation.containsOnlyAllowedCharacters())
                                    {
                                        tlRoomLocation.error = "Room location only contain alphabets, number and - or  _"
                                        return@isRoomDocumentExists
                                    }
                                    tlRoomLocation.error = null
                                    // If the room document does not exist, add it to the database
                                    addNewRoom(roomName,roomData)
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
    private fun addNewRoom(roomDocumentId:String, roomData:HashMap<String,String>)
    {
        // Insert the room document into the database by using repository insert method
        RoomRepository.insertRoomDocument(roomDocumentId,roomData,{
            // Success callback
            try {
                Log.d("TAG", "Room document added successfully with ID: $roomDocumentId")
                // Notify the listener about the addition of the room document
                roomAddListener?.onAdded(roomDocumentId,roomData)

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