package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_batch

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.dk.organizeu.adapter.BatchAdapter
import com.dk.organizeu.databinding.FragmentAddBatchBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.AcademicPojo.Companion.toAcademicPojo
import com.dk.organizeu.pojo.BatchPojo
import com.dk.organizeu.pojo.BatchPojo.Companion.toBatchPojo
import com.dk.organizeu.pojo.ClassPojo.Companion.toClassPojo
import com.dk.organizeu.pojo.SemesterPojo.Companion.toSemesterPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.BatchRepository
import com.dk.organizeu.repository.BatchRepository.Companion.insertBatchDocument
import com.dk.organizeu.repository.BatchRepository.Companion.isBatchDocumentExistsById
import com.dk.organizeu.repository.BatchRepository.Companion.isBatchDocumentExistsByName
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.DialogUtils
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AddBatchFragment : Fragment(), OnItemClickListener {

    companion object {
        fun newInstance() = AddBatchFragment()
        const val TAG = "OrganizeU-AddBatchFragment"
    }

    private lateinit var viewModel: AddBatchViewModel
    private lateinit var binding: FragmentAddBatchBinding
    private lateinit var progressDialog: CustomProgressDialog
    var batchDocumentId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_batch, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[AddBatchViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Apply operations within the binding and viewModel contexts
        binding.apply {
            viewModel.apply {
                try {
                    // Select the Academic item in the drawer menu
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                    // Disable UI elements related to subsequent selections until options are loaded
                    tlAcademicSem.isEnabled=false
                    tlAcademicClass.isEnabled=false
                    tlAcademicBatch.isEnabled=false
                    btnAddBatch.isEnabled=false

                    // Set selected values if previously set in AddAcademicFragment
                    if (AcademicDetailsFragment.academicType!=null && AcademicDetailsFragment.academicYear!=null)
                    {
                        if(academicYearSelectedItem==null)
                        {
                            academicYearSelectedItem = AcademicDetailsFragment.academicYear
                        }
                        if(academicTypeSelectedItem==null)
                        {
                            academicTypeSelectedItem = AcademicDetailsFragment.academicType
                        }
                        // Set text for academic year and type DropDown
                        actAcademicYear.setText(academicYearSelectedItem)
                        actAcademicType.setText(academicTypeSelectedItem)

                        // Set text for academic semester DropDown if available
                        if(academicSemSelectedItem!=null)
                        {
                            actAcademicSem.setText(academicSemSelectedItem)
                        }

                        // Set text for academic class DropDown if available
                        if(academicClassSelectedItem!=null)
                        {
                            actAcademicClass.setText(academicClassSelectedItem)
                        }

                        // Load options for academic year and type Drop Down
                        loadACTAcademicYear()
                        loadACTAcademicType()

                        // Initialize the Batch RecyclerView
                        initRecyclerView()

                        // Enable subsequent UI elements based on selections
                        if(academicTypeSelectedItem!=null)
                        {
                            tlAcademicSem.isEnabled=true
                        }
                        if(academicSemSelectedItem!=null)
                        {
                            tlAcademicClass.isEnabled=true
                        }
                        if(academicClassSelectedItem!=null)
                        {
                            tlAcademicBatch.isEnabled=true
                        }
                        if(etAcademicBatch.text.toString()!="")
                        {
                            btnAddBatch.isEnabled=true
                        }
                    }
                    // Load options for academic semester DropDown if academic year and type selections are available
                    if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null)
                    {
                        loadAcademicSemACTV()
                    }
                    // Load options for academic class DropDown
                    loadAcademicClassACTV()
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
                // Set a refresh listener to the SwipeRefreshLayout
                swipeRefresh.setOnRefreshListener {
                    // Refresh the Batch RecyclerView by reinitializing it
                    initRecyclerView()
                    // Hide the refreshing indicator after the Batch RecyclerView is refreshed
                    swipeRefresh.isRefreshing=false
                }
                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    // Get the selected academic year
                    academicYearSelectedItem = parent.getItemAtPosition(position).toString()

                    // Disable UI elements related to subsequent selections until options are loaded
                    tlAcademicType.isEnabled=false
                    tlAcademicSem.isEnabled=false
                    tlAcademicClass.isEnabled=false
                    tlAcademicBatch.isEnabled=false
                    btnAddBatch.isEnabled=false

                    // Clear and reset AutoCompleteTextViews and adapters for subsequent selections
                    clearACTAcademicType()
                    clearAcademicSemACTV()
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()

                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                            var academicIdEVEN:String? = null
                            var academicIdODD:String? = null
                            for(document in allAcademicDocument)
                            {
                                val academicPojo = document.toAcademicPojo()
                                if(AcademicType.EVEN.name==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                                {
                                    academicIdEVEN = academicPojo.id
                                }
                                else if(AcademicType.ODD.name==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                                {
                                    academicIdODD = academicPojo.id
                                }
                            }

                            // Add existing academic types to the academic type dropdown
                            if (academicIdEVEN!=null) {
                                academicTypeItemList.add(AcademicType.EVEN.name)
                            }
                            if (academicIdODD!=null) {
                                academicTypeItemList.add(AcademicType.ODD.name)
                            }
                            // Set up the adapter for the academic type drop down
                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            actAcademicType.setAdapter(academicTypeItemAdapter)

                            // Enable the academic type
                            tlAcademicType.isEnabled=true
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG,e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }

                    MainScope().launch{
                        // Wait for the asynchronous task to complete
                        job.join()
                    }
                }
                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Update the selected academic type
                        academicTypeSelectedItem = parent.getItemAtPosition(position).toString()

                        // Disable related UI elements until options are loaded
                        tlAcademicSem.isEnabled = false
                        tlAcademicClass.isEnabled = false
                        tlAcademicBatch.isEnabled = false
                        btnAddBatch.isEnabled = false

                        // Clear and load options for academic semester DropDown
                        clearAcademicSemACTV()
                        loadAcademicSemACTV()

                        // Clear options for academic class DropDown
                        clearAcademicClassACTV()

                        // Clear and notify data set changed for academic batch list
                        academicBatchList.clear()
                        academicBatchAdapter.notifyDataSetChanged()

                        // Enable academic semester DropDown
                        tlAcademicSem.isEnabled = true
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG, e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                actAcademicSem.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Update the selected academic semester
                        academicSemSelectedItem = parent.getItemAtPosition(position).toString()

                        // Disable related UI elements until options are loaded
                        tlAcademicClass.isEnabled = false
                        tlAcademicBatch.isEnabled = false
                        btnAddBatch.isEnabled = false

                        // Clear options for academic class DropDown
                        clearAcademicClassACTV()

                        // Clear and notify data set changed for academic batch list
                        academicBatchList.clear()
                        academicBatchAdapter.notifyDataSetChanged()

                        // Load options for academic class DropDown
                        loadAcademicClassACTV()

                        // Enable academic class DropDown
                        tlAcademicClass.isEnabled = true
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG, e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                actAcademicClass.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Update the selected academic class
                        academicClassSelectedItem = parent.getItemAtPosition(position).toString()

                        // Disable related UI elements until options are loaded
                        tlAcademicBatch.isEnabled = false
                        btnAddBatch.isEnabled = false

                        // Clear and notify data set changed for academic batch list
                        academicBatchList.clear()
                        academicBatchAdapter.notifyDataSetChanged()

                        // Initialize the RecyclerView
                        initRecyclerView()

                        // Enable academic batch input field
                        tlAcademicBatch.isEnabled = true
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG, e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                etAcademicBatch.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // Enable the Add Batch button if the batch text is not empty
                        btnAddBatch.isEnabled = s.toString() != ""
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                btnAddBatch.setOnClickListener {
                    try {// Check if all required fields are not null and the batch EditText is not blank or empty
                        if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null && academicSemSelectedItem!=null && academicClassSelectedItem!=null && etAcademicBatch.text!!.toString().isNotBlank() && etAcademicBatch.text!!.toString().isNotEmpty())
                        {

                            MainScope().launch(Dispatchers.Main)
                            {
                                val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                                var academicId:String? = null
                                for(document in allAcademicDocument)
                                {
                                    val academicPojo = document.toAcademicPojo()
                                    if(academicTypeSelectedItem==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                                    {
                                        academicId = academicPojo.id
                                        break
                                    }
                                }
                                val allsemesterDocuments = SemesterRepository.getAllSemesterDocuments(academicId!!)
                                var semId:String? = null
                                for(doc in allsemesterDocuments)
                                {
                                    val semesterPojo = doc.toSemesterPojo()
                                    if(semesterPojo.name == academicSemSelectedItem!!)
                                    {
                                        semId = semesterPojo.id
                                        break
                                    }
                                }

                                val allClassDocuments = ClassRepository.getAllClassDocuments(academicId,semId!!)
                                var classId:String? = null
                                for(doc in allClassDocuments)
                                {
                                    val classPojo = doc.toClassPojo()
                                    if(classPojo.name == academicClassSelectedItem!!)
                                    {
                                        classId = classPojo.id
                                        break
                                    }
                                }
                                batchDocumentId = etAcademicBatch.text.toString().trim().replace(Regex("\\s+")," ")
                                if(!batchDocumentId!!.containsOnlyAllowedCharacters())
                                {
                                    requireContext().showToast("Batch name only contain alphabets, number and - or  _ ")
                                    return@launch
                                }

                                // Check if the required document IDs are not null and the batch document ID is not "null"
                                if(classId!=null && batchDocumentId!="null")
                                {
                                    // Check if the required document IDs are not null and the batch document ID is not "null"
                                    isBatchDocumentExistsByName(academicId,
                                        semId, classId, batchDocumentId!!
                                    ){
                                        if(!it)
                                        {
                                            // If the batch document does not exist, insert it into the database
                                            val job = MainScope().launch(Dispatchers.IO) {
                                                try {
                                                    val newBatchPojo = BatchPojo(name = batchDocumentId!!)
                                                    // Call the insertBatchDocument function to add a new batch document to the database
                                                    insertBatchDocument(academicId, semId, classId, newBatchPojo,{
                                                        // Success Callback
                                                        try {
                                                            // Update UI and display a success message if the batch is added successfully
                                                            // Add the batch from the EditText to the list
                                                            academicBatchList.add(newBatchPojo)
                                                            // Notify the adapter that an item has been inserted at the last position in the list
                                                            academicBatchAdapter.notifyItemInserted(academicBatchAdapter.itemCount)
                                                            requireContext().showToast("Batch Added")
                                                            // Clear the text in the batch EditText to prepare for the next input
                                                            etAcademicBatch.setText("")
                                                        } catch (e: Exception) {
                                                            // Log any unexpected exceptions that occur
                                                            Log.e(TAG, e.message.toString())
                                                            // Display an unexpected error message to the user
                                                            requireContext().unexpectedErrorMessagePrint(e)
                                                            throw e
                                                        }
                                                    },{
                                                        // Error Callback
                                                        // Log any unexpected exceptions that occur
                                                        Log.e(TAG, it.message.toString())
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
                                            runBlocking {
                                                // wait for async task
                                                job.join()
                                            }
                                        }
                                        else{
                                            requireContext().showToast("Batch is exists")
                                        }
                                    }
                                }
                                else{
                                    requireContext().showToast("Invalid Input")
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
     * Initializes the Batch RecyclerView to display the list of academic batches.
     * Retrieves batch documents from the database based on the selected academic year, type, semester, and class.
     * Populates the Batch RecyclerView with the retrieved batch documents using a BatchAdapter.
     * Shows a progress bar while fetching batch documents from the database.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while fetching batch documents
                    showProgressBar(rvBatch,progressBar)

                    // Retrieve batch documents from the database in the background
                    MainScope().launch(Dispatchers.IO)
                    {
                        // Clear the existing list of academic batches
                        academicBatchList.clear()
                        // Construct the academic document ID
                        val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                        var academicId:String? = null
                        for(document in allAcademicDocument)
                        {
                            val academicPojo = document.toAcademicPojo()
                            if(academicTypeSelectedItem==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                            {
                                academicId = academicPojo.id
                                break
                            }
                        }
                        if(academicSemSelectedItem!=null && academicClassSelectedItem!=null)
                        {
                            try {
                                val allsemesterDocuments = SemesterRepository.getAllSemesterDocuments(academicId!!)
                                var semId:String? = null
                                for(doc in allsemesterDocuments)
                                {
                                    val semesterPojo = doc.toSemesterPojo()
                                    if(semesterPojo.name == academicSemSelectedItem!!)
                                    {
                                        semId = semesterPojo.id
                                        break
                                    }
                                }

                                val allClassDocuments = ClassRepository.getAllClassDocuments(academicId,semId!!)
                                var classId:String? = null
                                for(doc in allClassDocuments)
                                {
                                    val classPojo = doc.toClassPojo()
                                    if(classPojo.name == academicClassSelectedItem!!)
                                    {
                                        classId = classPojo.id
                                        break
                                    }
                                }
                                // Check if academic document ID, semester document ID, and class document ID are not null
                                if(classId!=null)
                                {
                                    // Retrieve batch documents from the BatchRepository
                                    val documents = BatchRepository.getAllBatchDocuments(academicId,semId, classId)
                                    // Add retrieved batch documents to the academicBatchList
                                    for (document in documents) {
                                        academicBatchList.add(document.toBatchPojo())
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        // Update UI on the main thread
                        withContext(Dispatchers.Main)
                        {
                            // Initialize the academicBatchAdapter with the academicBatchList
                            academicBatchAdapter = BatchAdapter(academicBatchList,this@AddBatchFragment)
                            // Set layout manager and adapter for the RecyclerView
                            rvBatch.layoutManager = LinearLayoutManager(requireContext())
                            rvBatch.adapter = academicBatchAdapter
                            // Delay to simulate a smoother UI experience
                            delay(500)
                            // Hide progress bar after fetching batch documents
                            hideProgressBar(rvBatch, progressBar)
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
     * Clears the selected academic type and resets the dropdown list of academic types.
     */
    private fun clearACTAcademicType() {
        binding.apply {
            viewModel.apply {
                try {
                    // Set the selected academic type to null
                    academicTypeSelectedItem = null
                    // Clear the list of academic types
                    academicTypeItemList.clear()
                    // Notify the adapter that the data set has changed
                    academicTypeItemAdapter.notifyDataSetChanged()
                    // Clear the text in the academic type DropDown
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
     * Clears the selected academic class and resets the dropdown list of academic classes.
     */
    private fun clearAcademicClassACTV() {
        binding.apply {
            viewModel.apply {
                try {
                    // Set the selected academic class to null
                    academicClassSelectedItem = null
                    // Clear the list of academic classes
                    academicClassItemList.clear()
                    // Notify the adapter that the data set has changed
                    academicClassItemAdapter.notifyDataSetChanged()
                    // Clear the text in the academic class DropDown
                    actAcademicClass.setText("")
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
     * Clears the selected academic semester and resets the dropdown list of academic semesters.
     */
    private fun clearAcademicSemACTV() {
        binding.apply {
            viewModel.apply {
                try {
                    // Set the selected academic semester to null
                    academicSemSelectedItem = null
                    // Clear the list of academic semesters
                    academicSemItemList.clear()
                    // Notify the adapter that the data set has changed
                    academicSemItemAdapter.notifyDataSetChanged()
                    // Clear the text in the academic semester AutoCompleteTextView
                    actAcademicSem.setText("")
                } catch (e: Exception) {
                    // Log and handle any unexpected exceptions
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }


    /**
     * Loads the academic years from the database and populates the dropdown list of academic years.
     */
    private fun loadACTAcademicYear()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO){
                    try {
                        // Clear the list of academic years
                        academicYearItemList.clear()
                        // Retrieve all academic documents from the repository
                        val documents = AcademicRepository.getAllAcademicDocuments()
                        // Iterate through each document
                        for (document in documents) {
                            // Split the document ID to extract the academic year
                            val academicPojo = document.toAcademicPojo()
                            // Add the academic year to the list if it's not already present
                            if(!academicYearItemList.contains(academicPojo.year))
                            {
                                academicYearItemList.add(academicPojo.year)
                            }
                        }
                        withContext(Dispatchers.Main){
                            try {
                                // Initialize the adapter with the list of academic years
                                academicYearItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearItemList)
                                // Set the adapter for the academic year dropdown
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
    }

    /**
     * Loads the academic types (even or odd) based on the selected academic year and populates the dropdown list of academic types.
     * This function also enables/disables the academic type text input layout based on the availability of academic types.
     */
    private fun loadACTAcademicType()
    {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the academic type text input layout initially
                    tlAcademicType.isEnabled = false
                    // Clear the list of academic types
                    academicTypeItemList.clear()
                    // Use a coroutine to perform database operations asynchronously
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                            var academicIdEVEN:String? = null
                            var academicIdODD:String? = null
                            for(document in allAcademicDocument)
                            {
                                val academicPojo = document.toAcademicPojo()
                                if(AcademicType.EVEN.name==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                                {
                                    academicIdEVEN = academicPojo.id
                                }
                                else if(AcademicType.ODD.name==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                                {
                                    academicIdODD = academicPojo.id
                                }
                            }
                            // If the even semester document exists, add "EVEN" to the list of academic types
                            if (academicIdEVEN!=null) {
                                academicTypeItemList.add(AcademicType.EVEN.name)
                            }
                            // If the odd semester document exists, add "ODD" to the list of academic types
                            if (academicIdODD!=null) {
                                academicTypeItemList.add(AcademicType.ODD.name)
                            }
                            // Initialize the adapter with the list of academic types
                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            // Set the adapter for the academic type DropDown
                            actAcademicType.setAdapter(academicTypeItemAdapter)
                            // Enable the academic type text input layout
                            tlAcademicType.isEnabled = true
                        } catch (e: Exception) {
                            // Log and handle any unexpected exceptions related to database operations
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }
                    // Wait for the coroutine job to complete before proceeding
                    MainScope().launch {
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
     * Loads the academic semesters based on the selected academic year and type, and populates the dropdown list of academic semesters.
     */
    private fun loadAcademicSemACTV()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO)
                {
                    try {
                        // Clear the list of academic semesters
                        academicSemItemList.clear()
                        val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                        var academicId:String? = null
                        for(document in allAcademicDocument)
                        {
                            val academicPojo = document.toAcademicPojo()
                            if(academicTypeSelectedItem==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                            {
                                academicId = academicPojo.id
                                break
                            }
                        }

                        // Check if the academic document ID is not null
                        if (academicId != null) {
                            // Retrieve all semester documents for the specified academic document ID
                            val documents = SemesterRepository.getAllSemesterDocuments(academicId)
                            // Iterate through the retrieved documents
                            for (document in documents) {
                                // Add the semester ID to the list of academic semesters
                                academicSemItemList.add(document.toSemesterPojo().name.toInt())
                            }
                        }
                        withContext(Dispatchers.Main) {
                            try {
                                // Initialize the adapter with the list of academic semesters
                                academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                                // Set the adapter for the academic semester DropDown
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
    }

    /**
     * Loads the academic classes based on the selected academic year, type, and semester, and populates the dropdown list of academic classes.
     */
    private fun loadAcademicClassACTV()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO)
                {
                    try {
                        // Clear the list of academic classes
                        academicClassItemList.clear()
                        val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                        var academicId:String? = null
                        for(document in allAcademicDocument)
                        {
                            val academicPojo = document.toAcademicPojo()
                            if(academicTypeSelectedItem==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                            {
                                academicId = academicPojo.id
                                break
                            }
                        }
                        if(academicSemSelectedItem!=null)
                        {
                            val allsemesterDocuments = SemesterRepository.getAllSemesterDocuments(academicId!!)
                            var semId:String? = null
                            for(doc in allsemesterDocuments)
                            {
                                val semesterPojo = doc.toSemesterPojo()
                                if(semesterPojo.name == academicSemSelectedItem!!)
                                {
                                    semId = semesterPojo.id
                                    break
                                }
                            }

                            if (semId != null) {
                                // Retrieve all class documents for the specified academic and semester document IDs
                                val documents = ClassRepository.getAllClassDocuments(academicId, semId)
                                // Iterate through the retrieved documents
                                for (document in documents) {
                                    // Add the class ID to the list of academic classes
                                    academicClassItemList.add(document.toClassPojo().name)
                                }
                            }
                        }


                        withContext(Dispatchers.Main) {
                            try {
                                // Initialize the adapter with the list of academic classes
                                academicClassItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicClassItemList)
                                // Set the adapter for the academic class DropDown
                                actAcademicClass.setAdapter(academicClassItemAdapter)
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
    }

    override fun onClick(position: Int) {
    }

    override fun onDeleteClick(position: Int) {
        viewModel.apply {

        val dialog = DialogUtils(requireContext()).build()

        dialog.setTitle("Delete Batch")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Batch and its data?")
            .show({
                MainScope().launch(Dispatchers.Main)
                {
                    // Call the Cloud Function to initiate delete operation
                    try {
                        val allAcademicDocument = AcademicRepository.getAllAcademicDocuments()
                        var academicId:String? = null
                        for(document in allAcademicDocument)
                        {
                            val academicPojo = document.toAcademicPojo()
                            if(academicTypeSelectedItem==academicPojo.type && academicYearSelectedItem==academicPojo.year)
                            {
                                academicId = academicPojo.id
                                break
                            }
                        }
                        val allSemesterDocuments = SemesterRepository.getAllSemesterDocuments(academicId!!)
                        var semId:String? = null
                        for(doc in allSemesterDocuments)
                        {
                            val semesterPojo = doc.toSemesterPojo()
                            if(semesterPojo.name == academicSemSelectedItem!!)
                            {
                                semId = semesterPojo.id
                                break
                            }
                        }

                        val allClassDocuments = ClassRepository.getAllClassDocuments(academicId,semId!!)
                        var classId:String? = null
                        for(doc in allClassDocuments)
                        {
                            val classPojo = doc.toClassPojo()
                            if(classPojo.name == academicClassSelectedItem!!)
                            {
                                classId = classPojo.id
                                break
                            }
                        }
                        // Get the batch document ID at the specified position from the Batch list
                        val batchPojo = viewModel.academicBatchList[position]

                        // Call the deleteBatch function with the academic document ID, semester document ID, class document ID, batch document ID, and a callback
                        deleteBatch(academicId,semId,classId!!,batchPojo.id){
                            try {
                                // Check if the deletion was successful
                                if(it)
                                {
                                    // If successful, remove the batch from the ViewModel's list and notify the adapter
                                    viewModel.academicBatchList.removeAt(position)
                                    viewModel.academicBatchAdapter.notifyItemRemoved(position)
                                    viewModel.academicBatchAdapter.notifyItemRangeChanged(position,viewModel.academicBatchAdapter.itemCount-position)
                                    requireContext().showToast("Batch deleted successfully.")
                                }
                                else{
                                    requireContext().showToast("Error occur while deleting batch.")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG,e.toString())
                                throw e
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.toString())
                        requireContext().showToast("Error occur while deleting batch.")
                    }
                    dialog.dismiss()
                }

            },{
                dialog.dismiss()
            })
        }


    }

    override fun onEditClick(position: Int) {
        requireContext().showToast("!Implement Soon!")
    }

    /**
     * Deletes the batch document associated with the specified academic, semester, class, and batch documents.
     * @param academicDocumentId The ID of the academic document containing the batch.
     * @param semesterDocumentId The ID of the semester document containing the batch.
     * @param classDocumentId The ID of the class document containing the batch.
     * @param batchDocumentId The ID of the batch document to delete.
     * @param isDeleted Callback function to notify the caller whether the deletion was successful.
     */
    private fun deleteBatch(academicDocumentId:String, semesterDocumentId:String, classDocumentId:String, batchDocumentId:String, isDeleted:(Boolean) -> Unit)
    {
        try {
            MainScope().launch(Dispatchers.IO){
                try {
                    // Call the deleteBatchDocument function from the repository
                    BatchRepository.deleteBatchDocument(academicDocumentId, semesterDocumentId, classDocumentId, batchDocumentId)

                    // Check if the batch document still exists after deletion
                    isBatchDocumentExistsById(academicDocumentId,semesterDocumentId,classDocumentId,batchDocumentId){
                        // Notify the caller whether the deletion was successful
                        isDeleted(!it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.toString())
                    throw e
                }
            }
        }
        catch (e: Exception){
            Log.e(TAG,e.toString())
            throw e
        }
    }
}