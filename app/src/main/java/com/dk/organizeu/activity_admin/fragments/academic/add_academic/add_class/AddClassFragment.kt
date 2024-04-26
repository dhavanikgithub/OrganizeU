package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_class

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_admin.fragments.academic.AcademicFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem.AddSemFragment
import com.dk.organizeu.adapter.ClassAdapter
import com.dk.organizeu.databinding.FragmentAddClassBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.firebase.key_mapping.ClassCollection
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.ClassRepository.Companion.isClassDocumentExists
import com.dk.organizeu.repository.SemesterRepository
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

class AddClassFragment : Fragment(), OnItemClickListener {

    companion object {
        var viewModel2: AddAcademicViewModel?=null
        fun newInstance(viewModel2: AddAcademicViewModel): AddClassFragment {
            AddClassFragment.viewModel2=viewModel2
            return AddClassFragment()
        }

        const val TAG = "OrganizeU-AddClassFragment"
    }

    private lateinit var viewModel: AddClassViewModel
    private lateinit var binding: FragmentAddClassBinding
    private lateinit var progressDialog: CustomProgressDialog
    var academicDocumentId:String? = null
    var semesterDocumentId:String? = null
    var classDocumentId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_class, container, false)
        binding = FragmentAddClassBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddClassViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                try {
                    // Select the appropriate item in the drawer menu
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                    // Disable the semester and class input fields and class add button
                    tlAcademicSem.isEnabled = false
                    tlAcademicClass.isEnabled = false
                    btnAddClass.isEnabled = false

                    // Check if academic type and year are provided from AddAcademicFragment
                    if (AddAcademicFragment.academicType != null && AddAcademicFragment.academicYear != null) {
                        // Initialize the selected academic year and type if they are not already set
                        if (academicYearSelectedItem == null) {
                            academicYearSelectedItem = AddAcademicFragment.academicYear
                        }
                        if (academicTypeSelectedItem == null) {
                            academicTypeSelectedItem = AddAcademicFragment.academicType
                        }

                        // Set the selected academic year and type in their respective drop down
                        actAcademicYear.setText(academicYearSelectedItem)
                        actAcademicType.setText(academicTypeSelectedItem)

                        // Set the selected semester if available
                        if (academicSemSelectedItem != null) {
                            actAcademicSem.setText(academicSemSelectedItem)
                        }

                        // Load academic year and type drop down list
                        loadactAcademicYear()
                        loadactAcademicType()

                        // Initialize the Class RecyclerView
                        initRecyclerView()

                        // Enable semester input field and class add button if academic type is set
                        if (academicTypeSelectedItem != null) {
                            tlAcademicSem.isEnabled = true
                        }

                        // Enable class input field if semester is set
                        if (academicSemSelectedItem != null) {
                            tlAcademicClass.isEnabled = true
                        }

                        // Enable class add button if class input field is not empty
                        if (etAcademicClass.text.toString() != "") {
                            btnAddClass.isEnabled = true
                        }
                    }

                    // Load semester drop down if academic year and type are set
                    if (academicYearSelectedItem != null && academicTypeSelectedItem != null) {
                        loadAcademicSemACTV()
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
                swipeRefresh.setOnRefreshListener {
                    // Refresh the RecyclerView data
                    initRecyclerView()
                    // Disable the refresh indicator after refreshing
                    swipeRefresh.isRefreshing = false
                }

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected academic year from the spinner
                        academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                        // Disable the academic type selection
                        tlAcademicType.isEnabled = false
                        // Disable the academic semester selection
                        tlAcademicSem.isEnabled = false
                        // Disable the academic class selection
                        tlAcademicClass.isEnabled = false
                        // Disable the button for adding a class
                        btnAddClass.isEnabled = false
                        // Clear the selected academic type
                        clearactAcademicType()
                        // Clear the selected academic semester
                        clearAcademicSemACTV()
                        // Clear the list of academic classes
                        academicClassList.clear()
                        // Notify the adapter that the data set has changed
                        academicClassAdapter.notifyDataSetChanged()

                        // Launch a coroutine to fetch academic types
                        val job = lifecycleScope.launch(Dispatchers.Main) {
                            try {
                                // Check if the even semester exists for the selected academic year
                                val evenExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                                // Add even semester to the list if it exists
                                if (evenExists) {
                                    academicTypeItemList.add(AcademicType.EVEN.name)
                                }

                                // Check if the odd semester exists for the selected academic year
                                val oddExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
                                // Add odd semester to the list if it exists
                                if (oddExists) {
                                    academicTypeItemList.add(AcademicType.ODD.name)
                                }

                                // Update the adapter with the available academic types
                                academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                                actAcademicType.setAdapter(academicTypeItemAdapter)
                                // Enable the academic type selection
                                tlAcademicType.isEnabled = true
                            } catch (e: Exception) {
                                // Log any unexpected exceptions that occur
                                Log.e(TAG,e.message.toString())
                                // Display an unexpected error message to the user
                                requireContext().unexpectedErrorMessagePrint(e)
                            }
                        }

                        // Ensure the job completes before proceeding
                        MainScope().launch{
                            job.join()
                        }
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG,e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected academic type from the spinner
                        academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                        // Disable the academic semester selection
                        tlAcademicSem.isEnabled = false
                        // Disable the academic class selection
                        tlAcademicClass.isEnabled = false
                        // Disable the button for adding a class
                        btnAddClass.isEnabled = false
                        // Clear the selected academic semester
                        clearAcademicSemACTV()
                        // Load the available academic semesters for the selected academic year and type
                        loadAcademicSemACTV()
                        // Clear the list of academic classes
                        academicClassList.clear()
                        // Notify the adapter that the data set has changed
                        academicClassAdapter.notifyDataSetChanged()
                        // Enable the academic semester selection
                        tlAcademicSem.isEnabled = true
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG,e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                actAcademicSem.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected academic semester from the AutoCompleteTextView
                        academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                        // Disable the academic class selection
                        tlAcademicClass.isEnabled = false
                        // Disable the button for adding a class
                        btnAddClass.isEnabled = false
                        // Clear the list of academic classes
                        academicClassList.clear()
                        // Notify the adapter that the data set has changed
                        academicClassAdapter.notifyDataSetChanged()
                        // Initialize the RecyclerView with the updated data
                        initRecyclerView()
                        // Enable the academic class selection
                        tlAcademicClass.isEnabled = true
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG, e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                etAcademicClass.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        try {
                            // Enable the button for adding a class if the class name is not empty
                            btnAddClass.isEnabled = s.toString().isNotEmpty()
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG, e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                btnAddClass.setOnClickListener {
                    try {
                        // Check if all necessary fields are selected and not empty
                        if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null && academicSemSelectedItem!=null && etAcademicClass.text!!.toString().isNotBlank() && etAcademicClass.text!!.toString().isNotEmpty())
                        {
                            // Construct the document IDs
                            academicDocumentId = "${academicYearSelectedItem}_${academicTypeSelectedItem}"
                            semesterDocumentId = academicSemSelectedItem
                            classDocumentId = etAcademicClass.text.toString()

                            // Check if the class document already exists
                            isClassDocumentExists(academicDocumentId!!,semesterDocumentId!!,classDocumentId!!){
                                try {
                                    // If the class document doesn't exist, insert it
                                    if(!it)
                                    {
                                        MainScope().launch(Dispatchers.IO)
                                        {
                                            val inputHashMap = hashMapOf(
                                                ClassCollection.CLASS.displayName to etAcademicClass.text.toString()
                                            )
                                            // Insert the class document into the database with the specified academic document ID, semester document ID, and class document ID,
                                            // along with the class data provided in the inputHashMap.
                                            ClassRepository.insertClassDocument(academicDocumentId!!,semesterDocumentId!!,classDocumentId!!,inputHashMap,{
                                                // On success, update the UI by adding the class to the list, notify the adapter of the insertion, and clear the text field for class input.
                                                try {
                                                    academicClassList.add(etAcademicClass.text.toString())
                                                    academicClassAdapter.notifyItemInserted(academicClassAdapter.itemCount)
                                                    etAcademicClass.setText("")
                                                    requireContext().showToast("Class Added")
                                                } catch (e: Exception) {
                                                    // Log any unexpected exceptions that occur
                                                    Log.e(TAG, e.message.toString())
                                                    // Display an unexpected error message to the user
                                                    requireContext().unexpectedErrorMessagePrint(e)
                                                    throw e
                                                }
                                            },{
                                                // Log any unexpected exceptions that occur
                                                Log.e(TAG, it.message.toString())
                                                // Display an unexpected error message to the user
                                                requireContext().unexpectedErrorMessagePrint(it)
                                                throw it
                                            })
                                        }
                                    }
                                    else{
                                        requireContext().showToast("Class is exists")
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
     * Initializes the Class RecyclerView with a list of class documents.
     * Fetches class documents from the repository based on the selected academic year, type, and semester.
     * Updates the UI by populating the Class RecyclerView with class data.
     */
    private fun initRecyclerView() {
        try {
            binding.apply {
                viewModel.apply {
                    // Show progress bar while fetching class documents
                    showProgressBar(rvClass,progressBar)
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            // Clear the existing list of class documents
                            academicClassList.clear()
                            // Generate document IDs based on selected parameters
                            academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                            semesterDocumentId = academicSemSelectedItem
                            // Fetch class documents from the repository if required document IDs not null
                            if(academicDocumentId!="null" && semesterDocumentId!=null)
                            {
                                val documents = ClassRepository.getAllClassDocuments(academicDocumentId!!,semesterDocumentId!!)
                                for (document in documents) {
                                    academicClassList.add(document.id)
                                }
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    // Set up the Class RecyclerView adapter and layout manager
                                    academicClassAdapter = ClassAdapter(academicClassList,this@AddClassFragment)
                                    rvClass.layoutManager = LinearLayoutManager(requireContext())
                                    rvClass.adapter = academicClassAdapter
                                    delay(500)
                                    // hiding the progress bar to ensure smooth UI transition
                                    hideProgressBar(rvClass,progressBar)
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
        } catch (e: Exception) {
            // Log any unexpected exceptions that occur
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }

    /**
     * Clears the selected academic type and its associated list in the UI.
     * Resets the adapter for the academic type autocomplete text view and clears its text field.
     */
    private fun clearactAcademicType() {
        binding.apply {
            viewModel.apply {
                try {
                    // Reset the selected academic type to null
                    academicTypeSelectedItem = null
                    // Clear the list of academic types
                    academicTypeItemList.clear()
                    // Notify the adapter that the dataset has changed
                    academicTypeItemAdapter.notifyDataSetChanged()
                    // Clear the text of the academic type dropdown
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
     * Clears the selected academic semester and its associated list in the UI.
     * Resets the adapter for the academic semester autocomplete text view and clears its text field.
     */
    private fun clearAcademicSemACTV() {
        // Apply changes to the binding and view model
        binding.apply {
            viewModel.apply {
                try {
                    // Reset the selected academic semester to null
                    academicSemSelectedItem = null
                    // Clear the list of academic semesters
                    academicSemItemList.clear()
                    // Notify the adapter that the dataset has changed
                    academicSemItemAdapter.notifyDataSetChanged()
                    // Clear the text of the academic semester dropdown
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
     * Loads the list of academic years and sets up the adapter for the academic year Dropdown.
     * This function retrieves academic documents from the repository, extracts the academic years, and populates the list.
     * It then updates the adapter for the academic year Dropdown with the updated list.
     */
    private fun loadactAcademicYear()
    {
        try {
            binding.apply {
                viewModel.apply {
                    // Clear the list of academic years
                    academicYearItemList.clear()
                    MainScope().launch(Dispatchers.IO){
                        try {
                            // Retrieve all academic documents from the repository
                            val documents = AcademicRepository.getAllAcademicDocuments()
                            // Iterate through the documents to extract academic years
                            for (document in documents) {
                                val academicItem = document.id.split('_')
                                // Add unique academic years to the list
                                if(!academicYearItemList.contains(academicItem[0]))
                                {
                                    academicYearItemList.add(academicItem[0])
                                }
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    // Update the adapter for the academic year dropdown
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


    /**
     * Loads the list of academic types and sets up the adapter for the academic type Dropdown.
     * This function checks the existence of academic documents for both even and odd semesters corresponding to the selected academic year.
     * It populates the list of academic types based on the availability of these documents and updates the adapter for the academic type Dropdown.
     */
    private fun loadactAcademicType()
    {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the academic type dropdown while loading data
                    tlAcademicType.isEnabled=false
                    // Clear the list of academic types
                    academicTypeItemList.clear()
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            // Check if documents exist for even semesters of the selected academic year
                            val evenExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                            if (evenExists) {
                                // Add "EVEN" to the list if documents exist
                                academicTypeItemList.add(AcademicType.EVEN.name)
                            }
                            // Check if documents exist for odd semesters of the selected academic year
                            val oddExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
                            if (oddExists) {
                                // Add "ODD" to the list if documents exist
                                academicTypeItemList.add(AcademicType.ODD.name)
                            }
                            // Update the adapter for the academic type dropdown
                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            actAcademicType.setAdapter(academicTypeItemAdapter)
                            // Re-enable the academic type dropdown after loading data
                            tlAcademicType.isEnabled=true
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG, e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }

                    MainScope().launch{
                        // Wait for the coroutine job to complete
                        job.join()
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
     * Loads the list of academic semesters and sets up the adapter for the academic semester dropdown.
     * This function retrieves semester documents based on the selected academic year and type, and populates the list of academic semesters accordingly.
     * It then updates the adapter for the academic semester dropdown.
     */
    private fun loadAcademicSemACTV()
    {
        try {
            binding.apply {
                viewModel.apply {
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            // Clear the list of academic semesters
                            academicSemItemList.clear()
                            // Construct the academic document ID based on the selected academic year and type
                            val academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                            // Retrieve semester documents from the repository
                            val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
                            for (document in documents) {
                                // Add each semester ID to the list
                                academicSemItemList.add(document.id.toInt())
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    // Update the adapter for the academic semester drop down
                                    academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                                    actAcademicSem.setAdapter(academicSemItemAdapter)
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
        } catch (e: Exception) {
            // Log any unexpected exceptions that occur
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }

    override fun onClick(position: Int) {
    }

    override fun onDeleteClick(position: Int) {
        val dialog = DialogUtils(requireContext()).build()

        dialog.setTitle("Delete Class")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Class and its data?")
            .show({
                // Call the Cloud Function to initiate delete operation
                try {
                    // Construct the academic document ID using selected year and type
                    val academicDocumentId = "${viewModel.academicYearSelectedItem}_${viewModel.academicTypeSelectedItem}"
                    // Get the selected semester document ID
                    val semesterDocumentId = viewModel.academicSemSelectedItem
                    // Get the class document ID at the specified position from the class list
                    val classDocumentId = viewModel.academicClassList[position]

                    // Call the deleteClass function with the academic document ID, semester document ID, class document ID, and a callback
                    deleteClass(academicDocumentId,semesterDocumentId!!,classDocumentId){
                        try {
                            // Check if the deletion was successful
                            if(it)
                            {
                                // If successful, remove the class from the class list and notify the adapter
                                viewModel.academicClassList.removeAt(position)
                                viewModel.academicClassAdapter.notifyItemRemoved(position)
                                // Show a toast message indicating successful deletion
                                requireContext().showToast("Class deleted successfully.")
                            }
                            else{
                                // If deletion was not successful, show a toast message indicating the error
                                requireContext().showToast("Error occur while deleting class.")
                            }
                        } catch (e: Exception) {
                            // Log any exceptions that occur during deletion
                            Log.e(AddSemFragment.TAG, e.toString())

                            // Re-throw the exception to propagate it further if needed
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    // Log any exceptions that occur outside the deletion process
                    Log.e(AddSemFragment.TAG,e.toString())
                    requireContext().showToast("Error occur while deleting class.")
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
     * Deletes the class document associated with the specified academic and semester documents.
     * @param academicDocumentId The ID of the academic document containing the class.
     * @param semesterDocumentId The ID of the semester document containing the class.
     * @param classDocumentId The ID of the class document to delete.
     * @param isDeleted Callback function to notify the caller whether the deletion was successful.
     */
    fun deleteClass(academicDocumentId:String, semesterDocumentId:String, classDocumentId:String, isDeleted:(Boolean) -> Unit){
        try {
            // Launch a coroutine in the IO dispatcher
            MainScope().launch(Dispatchers.IO){
                try {
                    // Call the deleteClassDocument function from the repository to delete class
                    ClassRepository.deleteClassDocument(academicDocumentId, semesterDocumentId, classDocumentId)
                    // Check if the class document still exists after deletion
                    isClassDocumentExists(academicDocumentId,semesterDocumentId,classDocumentId){
                        // Notify the caller whether the deletion was successful
                        isDeleted(!it)
                    }
                } catch (e: Exception) {
                    // Log any exceptions that occur during deletion
                    Log.e(TAG, e.toString())

                    // Re-throw the exception to propagate it further if needed
                    throw e
                }
            }
        }
        catch (e: Exception){
            // Log any exceptions that occur outside the coroutine scope
            Log.e(TAG, e.toString())

            // Re-throw the exception to propagate it further if needed
            throw e
        }
    }

}