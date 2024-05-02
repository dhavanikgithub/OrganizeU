package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AcademicDetailsFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AcademicDetailsViewModel
import com.dk.organizeu.adapter.SemesterAdapter
import com.dk.organizeu.databinding.FragmentAddSemBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.AcademicPojo.Companion.toAcademicPojo
import com.dk.organizeu.pojo.SemesterPojo
import com.dk.organizeu.pojo.SemesterPojo.Companion.toSemesterPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.DialogUtils
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddSemFragment : Fragment(), OnItemClickListener {

    companion object {
        var viewModel2:AcademicDetailsViewModel?=null
        fun newInstance(viewModel2:AcademicDetailsViewModel):AddSemFragment{
            AddSemFragment.viewModel2=viewModel2
            return AddSemFragment()
        }

        const val TAG = "OrganizeU-AddSemFragment"
    }

    private lateinit var viewModel: AddSemViewModel
    private lateinit var binding: FragmentAddSemBinding
    private lateinit var academicSemLayoutManager: LinearLayoutManager
    private lateinit var progressDialog: CustomProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_sem, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[AddSemViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                try {
                    // Select the 'Academic' menu item in the drawer menu of the parent activity
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)

                    // Disable the academic semester dropdown and 'Add Semester' button
                    tlAcademicSem.isEnabled = false
                    btnAddSem.isEnabled = false

                    // Check if academic type and year are available from the AddAcademicFragment
                    if (AcademicDetailsFragment.academicType != null && AcademicDetailsFragment.academicYear != null) {
                        // If academic year is not set, set it from AddAcademicFragment
                        if (academicYearSelectedItem == null) {
                            academicYearSelectedItem = AcademicDetailsFragment.academicYear
                        }
                        // If academic type is not set, set it from AddAcademicFragment
                        if (academicTypeSelectedItem == null) {
                            academicTypeSelectedItem = AcademicDetailsFragment.academicType
                        }
                        // Set the selected academic year and type in the corresponding dropdown fields
                        actAcademicYear.setText(academicYearSelectedItem)
                        actAcademicType.setText(academicTypeSelectedItem)

                        // Load academic year and type dropdown list
                        loadActAcademicYear()
                        loadActAcademicType()

                        // Initialize the Semester RecyclerView
                        initRecyclerView()

                        // Enable academic semester dropdown if academic type is selected
                        if (academicTypeSelectedItem != null) {
                            tlAcademicSem.isEnabled = true
                        }

                        // Enable 'Add Semester' button if academic semester is selected
                        if (academicSemSelectedItem != null) {
                            btnAddSem.isEnabled = true
                        }
                    }
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {

                // Set an onRefreshListener for the swipeRefresh layout to refresh the Semester RecyclerView data
                swipeRefresh.setOnRefreshListener {
                    // Call the initRecyclerView function to refresh the Semester RecyclerView
                    initRecyclerView()
                    // Set the refreshing state of swipeRefresh to false after data is refreshed
                    swipeRefresh.isRefreshing = false
                }

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Retrieve the selected academic year from the spinner
                        academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                        // Disable the academic semester and type drop down
                        tlAcademicSem.isEnabled = false
                        tlAcademicType.isEnabled = false
                        // Disable the add semester button
                        btnAddSem.isEnabled = false
                        // Clear the selected academic type and academic semester
                        clearactAcademicType()
                        clearAcademicSemACTV()
                        // Clear the academic semester list and notify the adapter
                        academicSemList.clear()
                        academicSemesterAdapter.notifyDataSetChanged()


                        val job = lifecycleScope.launch(Dispatchers.Main) {
                            try {
                                val academicEvenPojo = AcademicRepository.getAcademicPojoByYearAndType(academicYearSelectedItem!!,AcademicType.EVEN.name)
                                val academicOddPojo = AcademicRepository.getAcademicPojoByYearAndType(academicYearSelectedItem!!,AcademicType.ODD.name)

                                // If even semester document exists, add "EVEN" to the academicTypeItemList
                                if (academicEvenPojo != null) {
                                    academicTypeItemList.add(AcademicType.EVEN.name)
                                }

                                // If odd semester document exists, add "ODD" to the academicTypeItemList
                                if (academicOddPojo !=null) {
                                    academicTypeItemList.add(AcademicType.ODD.name)
                                }

                                // Set up the ArrayAdapter for the academic type dropdown with the updated academicTypeItemList
                                academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                                actAcademicType.setAdapter(academicTypeItemAdapter)
                                // Enable the academic type dropdown
                                tlAcademicType.isEnabled = true
                            } catch (e: Exception) {
                                // Log any unexpected exceptions that occur
                                Log.e(TAG,e.message.toString())
                                // Display an unexpected error message to the user
                                requireContext().unexpectedErrorMessagePrint(e)
                            }
                        }

                        MainScope().launch {
                            job.join() // Wait for the job to finish before proceeding
                        }
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG,e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                // Set an OnItemClickListener for actAcademicType
                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Retrieve the selected academic type from the academic semester dropdown
                        academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                        // Disable the academic semester dropdown and semester add button
                        tlAcademicSem.isEnabled = false
                        btnAddSem.isEnabled = false
                        // Clear the selected academic semester and related lists
                        clearAcademicSemACTV()
                        academicSemList.clear()
                        academicSemesterAdapter.notifyDataSetChanged()
                        // Start a progress dialog to indicate loading
                        progressDialog.start("Loading Semester...")
                        MainScope().launch(Dispatchers.IO) {
                            try {
                                val academicId:String? = AcademicRepository.getAcademicIdByYearAndType(viewModel.academicYearSelectedItem!!,viewModel.academicTypeSelectedItem!!)
                                // Retrieve all semester documents related to the academic document ID
                                val documents = SemesterRepository.getAllSemesterDocuments(academicId!!)
                                // Add the retrieved semester documents to the academicSemList
                                for (document in documents) {
                                    academicSemList.add(document.toSemesterPojo())
                                }

                                withContext(Dispatchers.Main) {
                                    try {
                                        // Notify the adapter of data changes in the academic semester dropdown list
                                        academicSemesterAdapter.notifyDataSetChanged()
                                        // Load the academic semesters into the academic semester dropdown list
                                        loadAcademicSemACTV()
                                        // Enable the academic semester dropdown
                                        tlAcademicSem.isEnabled = true
                                        // Stop the progress dialog
                                        progressDialog.stop()
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
                    }
                }


                actAcademicSem.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Retrieve the selected academic semester from the academic semester dropdown
                        academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                        // Enable the 'Add Semester' button since a semester is selected
                        btnAddSem.isEnabled = true
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG, e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                btnAddSem.setOnClickListener {
                    try {
                        // Check if academic type, year, and semester are selected
                        if(academicTypeSelectedItem!=null && academicYearSelectedItem!=null && academicSemSelectedItem!=null)
                        {
                            MainScope().launch(Dispatchers.IO){
                                try {
                                    val academicId:String? = AcademicRepository.getAcademicIdByYearAndType(viewModel.academicYearSelectedItem!!,viewModel.academicTypeSelectedItem!!)
                                    val newSemesterPojo = SemesterPojo(name = academicSemSelectedItem!!)

                                    SemesterRepository.insertSemesterDocuments(academicId!!,newSemesterPojo,{
                                        try {
                                            // Add semester to the list and notify adapter of the change
                                            academicSemList.add(newSemesterPojo)
                                            academicSemesterAdapter.notifyItemInserted(academicSemesterAdapter.itemCount)
                                            // Clear and reload the Academic Semester dropdown
                                            clearAcademicSemACTV()
                                            loadAcademicSemACTV()
                                            requireContext().showToast("Sem Added")
                                        } catch (e: Exception) {
                                            // Log any unexpected exceptions that occur
                                            Log.e(TAG, e.message.toString())
                                            // Display an unexpected error message to the user
                                            requireContext().unexpectedErrorMessagePrint(e)
                                            throw e
                                        }
                                    },{
                                        // Log any unexpected exceptions that occur
                                        Log.e(TAG,it.message.toString())
                                        // Display an unexpected error message to the user
                                        requireContext().unexpectedErrorMessagePrint(it)
                                        throw it
                                    })
                                } catch (e: Exception) {
                                    // Log any unexpected exceptions that occur
                                    Log.e(TAG, e.message.toString())
                                    // Display an unexpected error message to the user
                                    requireContext().unexpectedErrorMessagePrint(e)
                                    throw e
                                }
                            }

                        }
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG, e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

            }
        }
    }


    /**
     * Initializes the Semester RecyclerView to display semester data.
     * This function fetches semester data from the database and populates the Semester RecyclerView accordingly.
     * It also sets up the adapter and layout manager for the Semester RecyclerView.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while fetching semester data
                    showProgressBar(rvSemester,progressBar)
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            academicSemList.clear()
                            val academicId:String? = AcademicRepository.getAcademicIdByYearAndType(viewModel.academicYearSelectedItem!!,viewModel.academicTypeSelectedItem!!)
                            // Fetch all semester documents for the academic document ID
                            val documents = SemesterRepository.getAllSemesterDocuments(academicId!!)
                            // Add semester documents to the list
                            for (document in documents) {
                                academicSemList.add(document.toSemesterPojo())
                            }
                            withContext(Dispatchers.Main){
                                try {
                                    // Initialize the semester adapter and layout manager
                                    academicSemesterAdapter = SemesterAdapter(academicSemList,this@AddSemFragment)
                                    academicSemLayoutManager = LinearLayoutManager(requireContext())
                                    // Set adapter and layout manager to Semester RecyclerView
                                    rvSemester.layoutManager = academicSemLayoutManager
                                    rvSemester.adapter = academicSemesterAdapter
                                    // Load data into the Academic Semester dropdown
                                    loadAcademicSemACTV()
                                    delay(500)
                                    // Hide progress bar after data loading
                                    hideProgressBar(rvSemester,progressBar)
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
     * Clears the selected academic type and resets the corresponding views.
     * This function resets the selected academic type to null, clears the academic type item list,
     * notifies the adapter of changes, and sets the text of the Academic Type dropdown to an empty string.
     */
    private fun clearactAcademicType() {
        binding.apply {
            viewModel.apply {
                try {
                    // Clear the selected academic type
                    academicTypeSelectedItem = null
                    // Clear the academic type item list
                    academicTypeItemList.clear()
                    // Notify the adapter of changes
                    academicTypeItemAdapter.notifyDataSetChanged()
                    // Set the text of the Academic Type dropdown to an empty string
                    actAcademicType.setText("")
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
     * Clears the selected academic semester and resets the corresponding views.
     * This function resets the selected academic semester to null, clears the academic semester item list,
     * notifies the adapter of changes, and sets the text of the Academic Semester dropdown to an empty string.
     */
    private fun clearAcademicSemACTV() {
        binding.apply {
            viewModel.apply {
                try {
                    // Clear the selected academic semester
                    academicSemSelectedItem = null
                    // Clear the academic semester item list
                    academicSemItemList.clear()
                    // Notify the adapter of changes
                    academicSemItemAdapter.notifyDataSetChanged()
                    // Set the text of the Academic Semester dropdown to an empty string
                    actAcademicSem.setText("")
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
     * Loads the list of academic years into the Academic Year Dropdown.
     * This function clears the existing academic year item list, fetches all academic documents from the repository,
     * extracts unique academic years from the document IDs, and populates the Academic Year Dropdown.
     */
    private fun loadActAcademicYear()
    {
        binding.apply {
            viewModel.apply {
                try {
                    // Clear the existing academic year item list
                    academicYearItemList.clear()
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            // Fetch all academic documents from the repository
                            val documents = AcademicRepository.getAllAcademicDocuments()
                            // Extract unique academic years from the document IDs
                            for (document in documents) {
                                val academicPojo = document.toAcademicPojo()
                                if(!academicYearItemList.contains(academicPojo.year))
                                {
                                    academicYearItemList.add(academicPojo.year)
                                }
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    // Populate the Academic Year dropdown
                                    academicYearItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearItemList)
                                    actAcademicYear.setAdapter(academicYearItemAdapter)
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
     * Loads the list of academic types into the Academic Type Dropdown based on the selected academic year.
     * This function disables the Academic Type Dropdown, clears the existing academic type item list,
     * and then asynchronously checks for the existence of academic documents for even and odd semesters
     * corresponding to the selected academic year. If the documents exist, it adds the academic types (even and odd)
     * to the item list and enables the Academic Type Dropdown.
     */
    private fun loadActAcademicType() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the Academic Type Dropdown
                    tlAcademicType.isEnabled = false
                    // Clear the existing academic type item list
                    academicTypeItemList.clear()
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val academicEvenPojo = AcademicRepository.getAcademicPojoByYearAndType(academicYearSelectedItem!!,AcademicType.EVEN.name)
                            val academicOddPojo = AcademicRepository.getAcademicPojoByYearAndType(academicYearSelectedItem!!,AcademicType.ODD.name)


                            if (academicEvenPojo!=null) {
                                academicTypeItemList.add(AcademicType.EVEN.name)
                            }

                            if (academicOddPojo != null) {
                                academicTypeItemList.add(AcademicType.ODD.name)
                            }

                            // Populate the Academic Type Dropdown with the available academic types
                            academicTypeItemAdapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                academicTypeItemList
                            )
                            actAcademicType.setAdapter(academicTypeItemAdapter)
                            // Enable the Academic Type Dropdown
                            tlAcademicType.isEnabled = true
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG, e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }

                    MainScope().launch {
                        job.join() // Wait for async process
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
     * Loads the list of academic semesters into the Academic Semester Dropdown
     * based on the selected academic type (even or odd). If the selected academic type is even,
     * it populates the list with even semester numbers. If the selected academic type is odd,
     * it populates the list with odd semester numbers. The function then removes any existing semesters
     * from the list to prevent duplication and sets the adapter for the Academic Semester Dropdown.
     * Any unexpected exceptions are logged.
     */
    private fun loadAcademicSemACTV() {
        binding.apply {
            viewModel.apply {
                try {
                    // Clear the existing academic semester item list
                    academicSemItemList.clear()
                    // Populate the list based on the selected academic type
                    if (academicTypeSelectedItem == AcademicType.EVEN.name) {
                        academicSemItemList.addAll(UtilFunction.evenSemList)
                    } else if (academicTypeSelectedItem == AcademicType.ODD.name) {
                        academicSemItemList.addAll(UtilFunction.oddSemList)
                    }
                    // Remove any existing semesters from the list to prevent duplication
                    for (item in academicSemList) {
                        academicSemItemList.remove(item.name.toInt())
                    }
                    // Set the adapter for the Academic Semester Dropdown
                    academicSemItemAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        academicSemItemList
                    )
                    actAcademicSem.setAdapter(academicSemItemAdapter)
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


        dialog.setTitle("Delete Semester")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Semester and its data?")
            .show({
                // Call the Cloud Function to initiate delete operation
                try {
                    MainScope().launch(Dispatchers.IO)
                    {
                        // Get the semester at the specified position from the semester list
                        val semester = viewModel.academicSemList[position]
                        val academicId:String? = AcademicRepository.getAcademicIdByYearAndType(viewModel.academicYearSelectedItem!!,viewModel.academicTypeSelectedItem!!)


                        // Call the deleteSemester function with the academic document ID, semester, and a callback
                        deleteSemester(academicId!!,semester.id){
                            MainScope().launch(Dispatchers.Main)
                            {
                                try {
                                    // Check if the deletion was successful
                                    if(it)
                                    {
                                        // If successful, remove the semester from the semester list and notify the adapter
                                        viewModel.academicSemList.removeAt(position)
                                        viewModel.academicSemesterAdapter.notifyItemRemoved(position)
                                        viewModel.academicSemesterAdapter.notifyItemRangeChanged(position,viewModel.academicSemesterAdapter.itemCount-position)
                                        // Reload the Academic Semester dropdown
                                        loadAcademicSemACTV()
                                        // Show a toast message indicating successful deletion
                                        requireContext().showToast("Semester deleted successfully.")
                                    }
                                    else{
                                        // If deletion was not successful, show a toast message indicating the error
                                        requireContext().showToast("Error occur while deleting semester.")
                                    }
                                } catch (e: Exception) {
                                    // Log any exceptions that occur during deletion
                                    Log.e(TAG,e.toString())
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    // Log any exceptions that occur outside the deletion process
                    Log.e(TAG,e.toString())
                }
                dialog.dismiss()
            },{
                dialog.dismiss()
            })


    }

    override fun onEditClick(position: Int) {
        requireContext().showToast("!Implement Soon!")
    }

    /**
     * Deletes the semester document associated with the specified academic document.
     * @param academicDocumentId The ID of the academic document containing the semester.
     * @param semesterDocumentId The ID of the semester document to delete.
     * @param isDeleted Callback function to notify the caller whether the deletion was successful.
     */
    fun deleteSemester(academicDocumentId:String, semesterDocumentId: String,isDeleted:(Boolean) -> Unit){
        try {
            // Launch a coroutine in the IO dispatcher
            MainScope().launch(Dispatchers.IO){
                try {
                    // Call the deleteSemesterDocument function from the repository to delete semester
                    SemesterRepository.deleteSemesterDocument(academicDocumentId,semesterDocumentId)
                    // Check if the semester document still exists after deletion
                    SemesterRepository.isSemesterDocumentExistsById(academicDocumentId,semesterDocumentId){
                        // Notify the caller whether the deletion was successful
                        isDeleted(!it)
                    }

                } catch (e: Exception) {
                    // Log any exceptions that occur during deletion
                    Log.e(TAG, e.toString())

                    // Show a toast message for deletion error
                    withContext(Dispatchers.Main) {
                        requireContext().showToast("Error occurred while deleting semester.")
                    }

                    // Re-throw the exception to propagate it further if needed
                    throw e
                }
            }
        } catch (e: Exception) {
            // Log any exceptions that occur outside the coroutine scope
            Log.e(TAG, e.toString())

            // Re-throw the exception to propagate it further if needed
            throw e
        }
    }


}