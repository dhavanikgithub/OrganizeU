package com.dk.organizeu.activity_admin.fragments.academic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_admin.dialog.AddAcademicDialog
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.adapter.AcademicAdapter
import com.dk.organizeu.databinding.FragmentAcademicBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AcademicFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = AcademicFragment()
        const val TAG = "OrganizeU-AcademicFragment"
    }

    private lateinit var viewModel: AcademicViewModel
    private lateinit var binding: FragmentAcademicBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_academic, container, false)
        binding = FragmentAcademicBinding.bind(view)
        viewModel = ViewModelProvider(this)[AcademicViewModel::class.java]
        return view
    }

    override fun onResume() {
        super.onResume()
        /**
         * Sets the navigation drawer menu item to the academic section when the fragment resumes.
         * If an unexpected exception occurs, it is logged, and an unexpected error message is displayed.
         */
        binding.apply {
            viewModel.apply {
                try {
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString()) // Logs any unexpected exceptions.
                    requireContext().unexpectedErrorMessagePrint(e) // Displays an unexpected error message.
                }
            }
        }
    }

    /**
     * Overrides the onViewCreated method to initialize the view components and set up event listeners.
     * - Sets up a progress dialog for the academic fragment.
     * - Initializes the recycler view.
     * - Sets up a click listener for the "Add Academic" button to show a custom dialog fragment.
     * - Observes changes in the academic list LiveData and notifies the adapter when changes occur.
     * - Sets up a swipe refresh listener to refresh the recycler view data.
     * If an unexpected exception occurs during any of these operations, it is logged, and an unexpected error message is displayed.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    progressDialog = CustomProgressDialog(requireContext()) // Initializes the progress dialog.
                    initRecyclerView() // Initializes the recycler view.
                    btnAddAcademic.setOnClickListener { // Sets up a click listener for the "Add Academic" button.
                        val dialogFragment = AddAcademicDialog()
                        dialogFragment.show(childFragmentManager, "customDialog")
                    }
                    academicList.observe(viewLifecycleOwner){ // Observes changes in the academic list LiveData.
                        academicAdapter.notifyDataSetChanged() // Notifies the adapter when changes occur.
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString()) // Logs any unexpected exceptions.
                    requireContext().unexpectedErrorMessagePrint(e) // Displays an unexpected error message.
                }

                /**
                 * Sets up a swipe refresh listener for the swipeRefresh view.
                 * When the user performs a swipe-to-refresh action:
                 * - Calls the initRecyclerView function to refresh the academic recyclerview data.
                 * - Sets the refreshing state of the swipeRefresh view to false to indicate that the refresh action has been completed.
                 */
                swipeRefresh.setOnRefreshListener {
                    initRecyclerView() // Refreshes the academic recyclerview data.
                    swipeRefresh.isRefreshing = false // Sets refreshing state to false after refreshing.
                }
            }
        }
    }

    /**
     * Initializes the RecyclerView to display academic items.
     * This function sets up the RecyclerView, its adapter, and listens for changes in the academic data.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while loading data
                    showProgressBar(rvAcademic,progressBar)
                    // Clear existing data in the academic list
                    academicList.value!!.clear()
                    // Initialize the adapter with the academic list and click listener
                    academicAdapter = AcademicAdapter(academicList.value!!,this@AcademicFragment)
                    // Set Academic RecyclerView layout manager and adapter
                    rvAcademic.layoutManager = LinearLayoutManager(requireContext())
                    rvAcademic.adapter = academicAdapter
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            // Listen for changes in the academic collection
                            AcademicRepository.academicCollectionRef().addSnapshotListener { value, error ->
                                try {
                                    // Handle errors
                                    if (error != null) {
                                        return@addSnapshotListener
                                    }
                                    // Process document changes
                                    if (value != null && !value.isEmpty) {
                                        for (change in value.documentChanges) {
                                            val documentId = change.document.id
                                            val academicItem = documentId.split('_')
                                            val academicPojo = AcademicPojo("${academicItem[0]}", "${academicItem[1]}")
                                            when(change.type)
                                            {
                                                DocumentChange.Type.ADDED -> {
                                                    // Add the new academic item
                                                    academicList.value!!.add(academicPojo)
                                                    academicList.value = academicList.value
                                                }
                                                DocumentChange.Type.MODIFIED -> {
                                                    // Update the modified academic item
                                                    if(academicList.value!!.contains(academicPojo))
                                                    {
                                                        val index = academicList.value!!.indexOf(academicPojo)
                                                        academicList.value!![index] = academicPojo
                                                        academicList.value = academicList.value
                                                    }
                                                }
                                                DocumentChange.Type.REMOVED -> {
                                                    // Remove the deleted academic item
                                                    if(academicList.value!!.contains(academicPojo))
                                                    {
                                                        academicList.value!!.remove(academicPojo)
                                                        academicList.value = academicList.value
                                                    }
                                                }
                                            }

                                        }
                                    }
                                    // Hide progress bar after data loading
                                    hideProgressBar(rvAcademic,progressBar)
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

    /**
     * This function is a callback invoked when a document is added.
     * It implements the onAdded function of the AddDocumentListener interface.
     *
     * @param documentId The ID of the added document.
     * @param documentData The data of the added document stored in a HashMap.
     */
    override fun onAdded(documentId: String, documentData: HashMap<String, String>) {
        binding.apply {
            viewModel.apply {
                try {
                    // Extracting academic information from the document ID
                    val academicItem = documentId.split('_')

                    // Creating an AcademicPojo object and adding it to the academicList LiveData
                    academicList.value!!.add(AcademicPojo("${academicItem[0]}", "${academicItem[1]}"))
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(AddAcademicFragment.TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }

    /**
     * This function is a callback invoked when an item is clicked in the academic RecyclerView.
     * It implements the onClick function of the OnItemClickListener interface.
     *
     * @param position The position of the clicked item in the RecyclerView.
     */
    override fun onClick(position: Int) {
        binding.apply {
            viewModel.apply {
                try {
                    // Creating a Bundle to pass data to the destination fragment
                    val bundle = Bundle().apply {
                        // Extracting academic year and type information from the clicked item
                        putString("academic_year", "${academicList.value!![position].academic}")
                        putString("academic_type", "${academicList.value!![position].sem}")
                    }

                    // Navigating to the addAcademicFragment with the provided data
                    findNavController().navigate(R.id.addAcademicFragment, bundle)
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(AddAcademicFragment.TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }

    /**
     * This function is a callback invoked when the delete button of an item is clicked in the academic RecyclerView.
     * It implements the onDeleteClick function of the OnItemClickListener interface.
     *
     * @param position The position of the item whose delete button is clicked in the RecyclerView.
     */
    override fun onDeleteClick(position: Int) {

    }
}