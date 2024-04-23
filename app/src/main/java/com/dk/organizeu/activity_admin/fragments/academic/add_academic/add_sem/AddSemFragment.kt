package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.adapter.SemAdapter
import com.dk.organizeu.databinding.FragmentAddSemBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.firebase.key_mapping.SemesterCollection
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddSemFragment : Fragment() {

    companion object {
        var viewModel2:AddAcademicViewModel?=null
        fun newInstance(viewModel2:AddAcademicViewModel):AddSemFragment{
            AddSemFragment.viewModel2=viewModel2
            return AddSemFragment()
        }

        const val TAG = "OrganizeU-AddSemFragment"
    }

    private lateinit var viewModel: AddSemViewModel
    private lateinit var binding: FragmentAddSemBinding
    private lateinit var academicSemLayoutManager: LinearLayoutManager
    private lateinit var progressDialog: CustomProgressDialog
    var academicDocumentId:String? = null
    var semesterDocumentId:String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_sem, container, false)
        binding = FragmentAddSemBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddSemViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
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
                    if (AddAcademicFragment.academicType != null && AddAcademicFragment.academicYear != null) {
                        // If academic year is not set, set it from AddAcademicFragment
                        if (academicYearSelectedItem == null) {
                            academicYearSelectedItem = AddAcademicFragment.academicYear
                        }
                        // If academic type is not set, set it from AddAcademicFragment
                        if (academicTypeSelectedItem == null) {
                            academicTypeSelectedItem = AddAcademicFragment.academicType
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
                        academicSemAdapter.notifyDataSetChanged()


                        val job = lifecycleScope.launch(Dispatchers.Main) {
                            try {
                                // Check if academic documents for both even and odd semesters exist for the selected academic year
                                val evenExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                                val oddExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")

                                // If even semester document exists, add "EVEN" to the academicTypeItemList
                                if (evenExists) {
                                    academicTypeItemList.add(AcademicType.EVEN.name)
                                }

                                // If odd semester document exists, add "ODD" to the academicTypeItemList
                                if (oddExists) {
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
                        academicSemAdapter.notifyDataSetChanged()
                        // Start a progress dialog to indicate loading
                        progressDialog.start("Loading Semester...")
                        MainScope().launch(Dispatchers.IO) {
                            try {
                                // Construct the academic document ID using the selected year and type
                                academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                                if (academicDocumentId != null) {
                                    // Retrieve all semester documents related to the academic document ID
                                    val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId!!)
                                    // Add the retrieved semester documents to the academicSemList
                                    for (document in documents) {
                                        academicSemList.add(document.id)
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    try {
                                        // Notify the adapter of data changes in the academic semester dropdown list
                                        academicSemAdapter.notifyDataSetChanged()
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
                                    // Construct academic and semester document IDs
                                    academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                                    semesterDocumentId = academicSemSelectedItem

                                    // Check if document IDs are not null
                                    if(academicDocumentId!=null && semesterDocumentId!=null)
                                    {
                                        // Construct inputHashMap with semester data
                                        val inputHashMap = hashMapOf(
                                            SemesterCollection.SEMESTER.displayName to academicSemSelectedItem!!
                                        )
                                        // Insert semester documents into the database
                                        SemesterRepository.insertSemesterDocuments(academicDocumentId!!,semesterDocumentId!!, inputHashMap,{
                                            try {
                                                // Add semester to the list and notify adapter of the change
                                                academicSemList.add(academicSemSelectedItem!!)
                                                academicSemAdapter.notifyItemInserted(academicSemAdapter.itemCount)
                                                // Clear and reload the Academic Semester dropdown
                                                clearAcademicSemACTV()
                                                loadAcademicSemACTV()
                                                Toast.makeText(requireContext(),"Sem Added",Toast.LENGTH_SHORT).show()
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
                            // Construct academic document ID
                            academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                            // Check if academic document ID is not null
                            if(academicDocumentId!=null)
                            {
                                // Fetch all semester documents for the academic document ID
                                val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId!!)
                                // Add semester documents to the list
                                for (document in documents) {
                                    academicSemList.add(document.id)
                                }
                            }
                            withContext(Dispatchers.Main){
                                try {
                                    // Initialize the semester adapter and layout manager
                                    academicSemAdapter = SemAdapter(academicSemList)
                                    academicSemLayoutManager = LinearLayoutManager(requireContext())
                                    // Set adapter and layout manager to Semester RecyclerView
                                    rvSemester.layoutManager = academicSemLayoutManager
                                    rvSemester.adapter = academicSemAdapter
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
                                val academicItem = document.id.split('_')
                                if(!academicYearItemList.contains(academicItem[0]))
                                {
                                    academicYearItemList.add(academicItem[0])
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
                            // Check for the existence of academic documents for even semester
                            val evenExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                            if (evenExists) {
                                academicTypeItemList.add(AcademicType.EVEN.name)
                            }

                            // Check for the existence of academic documents for odd semester
                            val oddExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
                            if (oddExists) {
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
                        academicSemItemList.remove(item.toInt())
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


}