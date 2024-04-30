package com.dk.organizeu.activity_admin.fragments.rooms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.dialog.AddRoomDialog
import com.dk.organizeu.activity_admin.fragments.faculty.FacultyFragment
import com.dk.organizeu.adapter.RoomAdapter
import com.dk.organizeu.databinding.FragmentRoomsBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.EditDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.repository.RoomRepository.Companion.roomDocumentToRoomObj
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.DialogUtils
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomsFragment : Fragment(), AddDocumentListener, OnItemClickListener, EditDocumentListener {

    companion object {
        fun newInstance() = RoomsFragment()
        const val TAG = "OrganizeU-RoomsFragment"
    }

    private lateinit var viewModel: RoomsViewModel
    private lateinit var binding: FragmentRoomsBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_rooms, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[RoomsViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db= FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    // Attempt to initialize the Rooms RecyclerView
                   initRecyclerView()
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(FacultyFragment.TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
                swipeRefresh.setOnRefreshListener {
                    try {
                        // Attempt to reinitialize the Rooms RecyclerView when user do swipe refresh
                        initRecyclerView()
                    } catch (e: Exception) {
                        // Log any exceptions that occur
                        Log.e(FacultyFragment.TAG, e.message.toString())

                        // Print an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                    // Set refreshing state to false to indicate that the refresh is complete
                    swipeRefresh.isRefreshing=false
                }

                btnAddRoom.setOnClickListener {
                    try {
                        // Create an instance of the AddRoomDialog
                        val dialogFragment = AddRoomDialog(null)
                        dialogFragment.isCancelable=false
                        // Show the dialog using childFragmentManager
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        // If any exception occurs, log the error message
                        Log.e(TAG, e.message.toString())

                        // Print an unexpected error message using a custom function
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

            }
        }
    }

    /**
     * Initializes the RecyclerView by fetching data from the repository and setting up the adapter.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while fetching data
                    showProgressBar(rvRooms, progressBar)

                    // Use MainScope to launch a coroutine in IO dispatcher
                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear the existing list of roomPojoList
                            roomPojoList.clear()

                            // Retrieve all room documents from the repository
                            val documents = RoomRepository.getAllRoomDocument()

                            // Convert room documents to Room objects and add them to the list
                            for (document in documents) {
                                val roomItem = roomDocumentToRoomObj(document)
                                roomPojoList.add(roomItem)
                            }

                            // Switch to Main dispatcher to update UI
                            withContext(Dispatchers.Main) {
                                try {
                                    // Initialize RoomAdapter with the updated list and set it to Rooms RecyclerView
                                    roomAdapter = RoomAdapter(roomPojoList, this@RoomsFragment)
                                    rvRooms.layoutManager = LinearLayoutManager(requireContext())
                                    rvRooms.adapter = roomAdapter
                                    delay(500)

                                    // Hide progress bar after RecyclerView setup
                                    hideProgressBar(rvRooms, progressBar)
                                } catch (e: Exception) {
                                    // If any exception occurs, log the error message
                                    Log.e(TAG, e.message.toString())

                                    // Print an unexpected error message using a custom function
                                    requireContext().unexpectedErrorMessagePrint(e)
                                    throw e
                                }
                            }
                        } catch (e: Exception) {
                            // If any exception occurs, log the error message
                            Log.e(TAG, e.message.toString())

                            // Print an unexpected error message using a custom function
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    // If any exception occurs, log the error message
                    Log.e(TAG, e.message.toString())

                    // Print an unexpected error message using a custom function
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }


    /**
     * Callback function triggered when a new room document is added.
     * This function is responsible for updating the UI after a new room is added.
     *
     * @param documentId The ID of the newly added document.
     * @param documentData A HashMap containing the data of the newly added document.
     */
    override fun onAdded(documentId: String, documentData: HashMap<String, String>) {
        binding.apply {
            viewModel.apply {
                try {
                    // Convert document data to a Room object
                    val roomItem = roomDocumentToRoomObj(documentId, documentData)

                    // Add the new room to the list
                    roomPojoList.add(roomItem)

                    // Notify the adapter of the newly inserted item
                    roomAdapter.notifyItemInserted(roomAdapter.itemCount)
                    requireContext().showToast("Room Added Successfully")
                } catch (e: Exception) {
                    // Log any exceptions that occur
                    Log.e(TAG, e.message.toString())

                    // Print an unexpected error message using a custom function
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Callback function triggered when an item in the Rooms RecyclerView is clicked.
     * This function can be implemented to handle click events on Rooms RecyclerView items.
     *
     * @param position The position of the clicked item in the Rooms RecyclerView.
     */
    override fun onClick(position: Int) {
        // Implement the desired behavior when an item is clicked.
        // You can use the 'position' parameter to identify which item was clicked.
    }

    /**
     * Callback function triggered when the delete button of an item in the Rooms RecyclerView is clicked.
     * This function can be implemented to handle delete button click events on Rooms RecyclerView items.
     *
     * @param position The position of the item whose delete button was clicked in the Rooms RecyclerView.
     */
    override fun onDeleteClick(position: Int) {
        val dialog = DialogUtils(requireContext()).build()

        dialog.setTitle("Delete Room")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Room and its data?")
            .show({
                // Call the Cloud Function to initiate delete operation
                try {

                    // Get the room document ID at the specified position from the Room list
                    val room = viewModel.roomPojoList[position]

                    deleteRoom(room.name){
                        try {
                            if(it)
                            {
                                viewModel.roomPojoList.removeAt(position)
                                viewModel.roomAdapter.notifyItemRemoved(position)
                                requireContext().showToast("Room deleted successfully.")
                            }
                            else{
                                requireContext().showToast("Error occur while deleting room.")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.toString())
                            throw e
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG,e.toString())
                    requireContext().showToast("Error occur while deleting room.")
                }
                dialog.dismiss()
            },{
                dialog.dismiss()
            })

    }

    override fun onEditClick(position: Int) {
        try {
            // Create an instance of the AddRoomDialog
            val dialogFragment = AddRoomDialog(viewModel.roomPojoList[position])
            dialogFragment.isCancelable=false
            // Show the dialog using childFragmentManager
            dialogFragment.show(childFragmentManager, "customDialog")
        } catch (e: Exception) {
            // If any exception occurs, log the error message
            Log.e(TAG, e.message.toString())

            // Print an unexpected error message using a custom function
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }

    fun deleteRoom(roomDocumentId:String, isDeleted:(Boolean) -> Unit)
    {
        try {
            MainScope().launch(Dispatchers.IO)
            {
                try {
                    RoomRepository.deleteRoomDocument(roomDocumentId)
                    RoomRepository.isRoomDocumentExists(roomDocumentId){
                        isDeleted(!it)
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun onEdited(
        oldDocumentId: String,
        newDocumentId: String,
        documentData: HashMap<String, String>
    ) {
        try {
            val index = viewModel.roomPojoList.indexOfFirst {
                it.name == oldDocumentId
            }

            viewModel.roomPojoList.removeAt(index)
            viewModel.roomPojoList.add(index,roomDocumentToRoomObj(newDocumentId,documentData))
            MainScope().launch(Dispatchers.Main)
            {
                viewModel.roomAdapter.notifyDataSetChanged()
                requireContext().showToast("Room Data Updated")
            }
        }
        catch (e: Exception)
        {
            requireContext().showToast("Room Data Update Failed")
        }
    }
}