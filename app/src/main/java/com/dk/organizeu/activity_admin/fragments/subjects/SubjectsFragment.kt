package com.dk.organizeu.activity_admin.fragments.subjects

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.dialog.AddSubjectDialog
import com.dk.organizeu.activity_admin.fragments.rooms.RoomsFragment
import com.dk.organizeu.adapter.SubjectAdapter
import com.dk.organizeu.databinding.FragmentSubjectsBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.SubjectRepository
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

class SubjectsFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = SubjectsFragment()
        const val TAG = "OrganizeU-SubjectsFragment"
    }

    private lateinit var viewModel: SubjectsViewModel
    private lateinit var binding: FragmentSubjectsBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_subjects, container, false)
        binding = FragmentSubjectsBinding.bind(view)
        viewModel =ViewModelProvider(this)[SubjectsViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db= FirebaseFirestore.getInstance()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    // Initialize the Subject RecyclerView
                    initRecyclerView()
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur during Subject RecyclerView initialization
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

                // Set a refresh listener for the swipeRefreshLayout
                swipeRefresh.setOnRefreshListener {
                    // When swipe refresh is triggered, reinitialize the Subject RecyclerView
                    initRecyclerView()
                    // Hide the swipe refresh indicator after refreshing
                    swipeRefresh.isRefreshing = false
                }

                btnAddSubject.setOnClickListener {
                    // Create an instance of the AddSubjectDialog
                    val dialogFragment = AddSubjectDialog()
                    try {
                        // Show the dialog using childFragmentManager
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur while showing the dialog
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

            }
        }
    }

    /**
     * Initializes the Subject RecyclerView by fetching subject data from the repository and setting up the adapter.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while fetching data
                    showProgressBar(rvSubjects, progressBar)


                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear the existing list of subjectPojoList
                            subjectPojoList.clear()

                            // Retrieve all subject documents from the repository
                            val documents = SubjectRepository.getAllSubjectDocuments()

                            // Convert subject documents to Subject objects and add them to the list
                            for (document in documents) {
                                val subjectItem = SubjectRepository.subjectDocumentToSubjectObj(document)
                                subjectPojoList.add(subjectItem)
                            }

                            // Switch to Main dispatcher to update UI
                            withContext(Dispatchers.Main) {
                                try {
                                    // Initialize SubjectAdapter with the updated list and set it to Subject RecyclerView
                                    subjectAdapter = SubjectAdapter(subjectPojoList, this@SubjectsFragment)
                                    rvSubjects.layoutManager = LinearLayoutManager(requireContext())
                                    rvSubjects.adapter = subjectAdapter
                                    delay(500)

                                    // Hide progress bar after Subject RecyclerView setup
                                    hideProgressBar(rvSubjects, progressBar)
                                } catch (e: Exception) {
                                    // Log any unexpected exceptions that occur
                                    Log.e(TAG,e.message.toString())
                                    // Display an unexpected error message to the user
                                    requireContext().unexpectedErrorMessagePrint(e)
                                    throw e
                                }
                            }
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG,e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }


    /**
     * Callback function triggered when a new subject document is added.
     * This function is responsible for updating the UI after a new subject is added.
     *
     * @param documentId The ID of the newly added subject document.
     * @param documentData A HashMap containing the data of the newly added subject document.
     */
    override fun onAdded(documentId: String, documentData: HashMap<String, String>) {
        binding.apply {
            viewModel.apply {
                try {
                    // Convert document data to a Subject object
                    val subjectItem = SubjectRepository.subjectDocumentToSubjectObj(documentId, documentData)

                    // Add the new subject to the list
                    subjectPojoList.add(subjectItem)

                    // Notify the adapter of the newly inserted item
                    subjectAdapter.notifyItemInserted(subjectAdapter.itemCount)
                    requireContext().showToast("Subject Added Successfully")
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }


    /**
     * Callback function triggered when an item in the Subject RecyclerView is clicked.
     * This function can be implemented to handle click events on Subject RecyclerView items.
     *
     * @param position The position of the clicked item in the Subject RecyclerView.
     */
    override fun onClick(position: Int) {
        // You can use the 'position' parameter to identify which item was clicked.
    }

    /**
     * Callback function triggered when the delete button of an item in the Subject RecyclerView is clicked.
     * This function can be implemented to handle delete button click events Subject RecyclerView items.
     *
     * @param position The position of the item whose delete button was clicked in the Subject RecyclerView.
     */
    override fun onDeleteClick(position: Int) {
        val dialog = DialogUtils(requireContext()).build()

        dialog.setTitle("Delete Subject")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Subject and its data?")
            .show({
                // Call the Cloud Function to initiate delete operation
                try {

                    // Get the room document ID at the specified position from the subject list
                    val subject = viewModel.subjectPojoList[position]

                    deleteSubject(subject.name){
                        try {
                            if(it)
                            {
                                viewModel.subjectPojoList.removeAt(position)
                                viewModel.subjectAdapter.notifyItemRemoved(position)
                                requireContext().showToast("Subject deleted successfully.")
                            }
                            else{
                                requireContext().showToast("Error occur while deleting subject.")
                            }
                        } catch (e: Exception) {
                            Log.e(RoomsFragment.TAG,e.toString())
                            throw e
                        }
                    }

                } catch (e: Exception) {
                    Log.e(RoomsFragment.TAG,e.toString())
                    requireContext().showToast("Error occur while deleting subject.")
                }
                dialog.dismiss()
            },{
                dialog.dismiss()
            })


    }

    override fun onEditClick(position: Int) {
        requireContext().showToast("!Implement Soon!")
    }

    fun deleteSubject(subjectDocumentId:String, isDeleted:(Boolean) -> Unit)
    {
        try {
            MainScope().launch(Dispatchers.IO)
            {
                try {
                    SubjectRepository.deleteSubjectDocument(subjectDocumentId)
                    SubjectRepository.isSubjectDocumentExists(subjectDocumentId){
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
}