package com.dk.organizeu.activity_admin.fragments.faculty

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
import com.dk.organizeu.activity_admin.dialog.AddFacultyDialog
import com.dk.organizeu.activity_admin.fragments.subjects.SubjectsFragment
import com.dk.organizeu.adapter.FacultyAdapter
import com.dk.organizeu.databinding.FragmentFacultyBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.EditDocumentListener
import com.dk.organizeu.repository.FacultyRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.DialogUtils
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FacultyFragment : Fragment(), com.dk.organizeu.listener.OnItemClickListener,
    AddDocumentListener, EditDocumentListener {

    companion object {
        fun newInstance() = FacultyFragment()
        const val TAG = "OrganizeU-FacultyFragment"
    }

    private lateinit var viewModel: FacultyViewModel
    private lateinit var binding: FragmentFacultyBinding
    private lateinit var progressDialog: CustomProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view  = inflater.inflate(R.layout.fragment_faculty, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[FacultyViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    // Attempt to initialize the Faculty RecyclerView
                    initRecyclerView()
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }

                swipeRefresh.setOnRefreshListener {
                    try {
                        // Attempt to reinitialize the Faculty RecyclerView when user do swipe refresh
                        initRecyclerView()
                    } catch (e: Exception) {
                        // Log any exceptions that occur
                        Log.e(TAG, e.message.toString())

                        // Print an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }

                    // Set refreshing state to false to indicate that the refresh is complete
                    swipeRefresh.isRefreshing = false
                }

                btnAddFaculty.setOnClickListener {
                    // Create an instance of the AddFacultyDialog
                    val dialogFragment = AddFacultyDialog(null)
                    try {
                        // Show the dialog using childFragmentManager
                        dialogFragment.isCancelable=false
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur while showing the dialog
                        Log.e(SubjectsFragment.TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

            }
        }
    }



    /**
     * Initializes the RecyclerView to display faculty items.
     * This function fetches faculty documents from the repository and populates the Academic RecyclerView with the retrieved data.
     * It also handles UI operations such as showing/hiding progress bar and error handling.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while loading data
                    showProgressBar(rvFaculty, progressBar)

                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear existing faculty list
                            facultyList.clear()

                            // Fetch all faculty documents from the repository
                            val documents = FacultyRepository.getAllFacultyDocuments()

                            // Add faculty names from the documents to the faculty list
                            for (document in documents) {
                                facultyList.add(document.id)
                            }

                            withContext(Dispatchers.Main) {
                                try {
                                    // Initialize the adapter with the faculty list
                                    facultyAdapter = FacultyAdapter(facultyList,this@FacultyFragment)

                                    // Set Faculty RecyclerView layout manager and adapter
                                    rvFaculty.layoutManager = LinearLayoutManager(requireContext())
                                    rvFaculty.adapter = facultyAdapter

                                    delay(500)

                                    // Hide progress bar after data loading
                                    hideProgressBar(rvFaculty, progressBar)
                                } catch (e: Exception) {
                                    // Log any unexpected exceptions that occur
                                    Log.e(TAG, e.message.toString())
                                    // Display an unexpected error message to the user
                                    requireContext().unexpectedErrorMessagePrint(e)
                                    throw e
                                }
                            }
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG, e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }

    override fun onClick(position: Int) {

    }

    override fun onDeleteClick(position: Int) {
        val dialog = DialogUtils(requireContext()).build()

        dialog.setTitle("Delete Faculty")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Faculty and its data?")
            .show({
                // Call the Cloud Function to initiate delete operation
                try {
                    // Get the faculty document ID at the specified position from the Faculty list
                    val facultyDocumentId = viewModel.facultyList[position]

                    deleteFaculty(facultyDocumentId){
                        try {
                            if(it)
                            {
                                viewModel.facultyAdapter.itemDelete(position)
                                requireContext().showToast("Faculty deleted successfully.")
                            }
                            else{
                                requireContext().showToast("Error occur while deleting faculty.")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.toString())
                            throw e
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG,e.toString())
                    requireContext().showToast("Error occur while deleting faculty.")
                }
                dialog.dismiss()
            },{
                dialog.dismiss()
            })


    }

    override fun onEditClick(position: Int) {
        try {
            // Create an instance of the AddFacultyDialog
            val dialogFragment = AddFacultyDialog(viewModel.facultyList[position])
            // Show the dialog using childFragmentManager
            dialogFragment.isCancelable=false
            dialogFragment.show(childFragmentManager, "customDialog")
        } catch (e: Exception) {
            // Log and handle any exceptions that occur while showing the dialog
            Log.e(SubjectsFragment.TAG, e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }

    fun deleteFaculty(facultyDocumentId: String, isDeleted:(Boolean) -> Unit)
    {
        try {
            MainScope().launch(Dispatchers.IO)
            {
                try {
                    FacultyRepository.deleteFacultyDocument(facultyDocumentId)
                    FacultyRepository.isFacultyDocumentExists(facultyDocumentId){
                        isDeleted(!it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.toString())
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e(TAG,e.toString())
            throw e
        }
    }

    override fun onAdded(documentId: String, documentData: HashMap<String, String>) {
        try {
            viewModel.facultyAdapter.itemInsert(documentId)
            requireContext().showToast("Faculty Added")
        } catch (e: Exception) {
            Log.e(TAG,e.toString())
            requireContext().showToast("Operation failed to add faculty")
        }
    }

    override fun onEdited(
        oldDocumentId: String,
        newDocumentId: String,
        documentData: HashMap<String,String>
    ) {
        try {

            MainScope().launch(Dispatchers.Main)
            {
                viewModel.facultyAdapter.itemModify(oldDocumentId,newDocumentId)
                requireContext().showToast("Faculty Update Successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG,e.toString())
            requireContext().showToast("Operation failed to Edit faculty")
        }
    }


}