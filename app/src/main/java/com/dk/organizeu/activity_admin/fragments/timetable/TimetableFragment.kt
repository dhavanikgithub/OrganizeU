package com.dk.organizeu.activity_admin.fragments.timetable

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
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentTimetableBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.pojo.AcademicPojo.Companion.toAcademicPojo
import com.dk.organizeu.pojo.ClassPojo.Companion.toClassPojo
import com.dk.organizeu.pojo.SemesterPojo.Companion.toSemesterPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimetableFragment : Fragment() {

    companion object {
        fun newInstance() = TimetableFragment()
        const val TAG = "OrganizeU-TimetableFragment"
    }

    private lateinit var viewModel: TimetableViewModel
    private lateinit var binding: FragmentTimetableBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[TimetableViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                try {
                    // Load academic year drop-down menu
                    loadAcademicYearDropDown()

                    // Disable the "Go to Timetable" button initially
                    btnGoToTimetable.isEnabled = false

                    // Enable/disable academic type drop-down menu based on selection
                    if (selectedAcademicTypeItem == null) {
                        tlAcademicType.isEnabled = false
                    } else {
                        if (selectedAcademicYearItem != null) {
                            loadAcademicTypeDropDown()
                        }
                    }

                    // Enable/disable semester drop-down menu based on selection
                    if (selectedSemesterItem == null) {
                        tlAcademicSem.isEnabled = false
                    } else {
                        if (selectedAcademicYearItem != null && selectedAcademicTypeItem != null) {
                            loadSemesterDropDown()
                        }
                    }

                    // Enable/disable class drop-down menu based on selection
                    if (selectedClassItem == null) {
                        classTIL.isEnabled = false
                    } else {
                        if (selectedAcademicYearItem != null && selectedAcademicTypeItem != null && selectedSemesterItem != null) {
                            loadClassDropDown()
                        }
                    }

                    // Enable the "Go to Timetable" button if all selections are made
                    if (selectedAcademicYearItem != null && selectedAcademicTypeItem != null && selectedSemesterItem != null && selectedClassItem != null) {
                        btnGoToTimetable.isEnabled = true
                    }
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
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

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected academic year item
                        selectedAcademicYearItem = parent.getItemAtPosition(position).toString()

                        // Clear selections for related dropdown menus
                        clearAcademicType()
                        clearSemester()
                        clearClass()

                        // Load academic type dropdown menu based on the selected academic year
                        loadAcademicTypeDropDown()

                        // Disable semester and class dropdown menus
                        tlAcademicSem.isEnabled = false
                        classTIL.isEnabled = false

                        // Disable the "Go to Timetable" button
                        btnGoToTimetable.isEnabled = false
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected academic type item
                        selectedAcademicTypeItem = parent.getItemAtPosition(position).toString()

                        // Clear selections for related dropdown menus
                        clearSemester()
                        clearClass()

                        // Load semester dropdown menu based on the selected academic type
                        loadSemesterDropDown()

                        // Disable class dropdown menu
                        classTIL.isEnabled = false

                        // Disable the "Go to Timetable" button
                        btnGoToTimetable.isEnabled = false
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }



                actAcademicSem.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected academic semester item
                        selectedSemesterItem = parent.getItemAtPosition(position).toString()

                        // Clear selections for the class dropdown menu
                        clearClass()

                        // Load class dropdown menu based on the selected academic semester
                        loadClassDropDown()

                        // Disable the "Go to Timetable" button
                        btnGoToTimetable.isEnabled = false
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                classACTV.setOnItemClickListener { parent, view, position, id ->
                    try {
                        // Get the selected class item
                        selectedClassItem = parent.getItemAtPosition(position).toString()

                        // Enable the "Go to Timetable" button
                        btnGoToTimetable.isEnabled = true
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                btnGoToTimetable.setOnClickListener {
                    try {
                        // Check if all required selections are made
                        if (selectedAcademicYearItem != null && selectedAcademicTypeItem != null && selectedSemesterItem != null && selectedClassItem != null) {
                            // Create a bundle to pass selected data to the addTimetableFragment
                            val bundle = Bundle().apply {
                                putString("academic_year", selectedAcademicYearItem)
                                putString("academic_type", selectedAcademicTypeItem)
                                putString("academic_semester", selectedSemesterItem)
                                putString("academic_class", selectedClassItem)
                            }

                            // Navigate to the addTimetableFragment with the selected data
                            findNavController().navigate(R.id.addTimetableFragment, bundle)
                        }
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

            }
        }
    }


    /**
     * Clears the selection and data related to the academic semester.
     */
    fun clearSemester() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the academic semester dropdown menu
                    tlAcademicSem.isEnabled = false

                    // Clear the text in the academic semester dropdown text
                    actAcademicSem.setText("")

                    // Clear the semesterList data
                    semesterList.clear()

                    // Notify the adapter of changes in data if it's not null
                    if (semesterAdapter != null) {
                        semesterAdapter!!.notifyDataSetChanged()
                    }

                    // Reset the selected semester item
                    selectedSemesterItem = null
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Clears the selection and data related to the class.
     */
    fun clearClass() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the class dropdown menu
                    classTIL.isEnabled = false

                    // Clear the text in the class dropdown text
                    classACTV.setText("")

                    // Clear the classList data
                    classList.clear()

                    // Notify the adapter of changes in data if it's not null
                    if (classAdapter != null) {
                        classAdapter!!.notifyDataSetChanged()
                    }

                    // Reset the selected class item
                    selectedClassItem = null
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Clears the selection and data related to the academic type.
     */
    fun clearAcademicType() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the academic type dropdown menu
                    tlAcademicType.isEnabled = false

                    // Clear the text in the academic type dropdown text
                    actAcademicType.setText("")

                    // Clear the academicTypeList data
                    academicTypeList.clear()

                    // Notify the adapter of changes in data if it's not null
                    if (academicTypeAdapter != null) {
                        academicTypeAdapter!!.notifyDataSetChanged()
                    }

                    // Reset the selected academic type item
                    selectedAcademicTypeItem = null
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Loads academic year dropdown menu with available academic years.
     */
    fun loadAcademicYearDropDown() {
        try {
            binding.apply {
                viewModel.apply {
                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear the existing academic year list
                            academicYearList.clear()

                            // Retrieve academic documents from the repository
                            val documents = AcademicRepository.getAllAcademicDocuments()

                            // Extract and add unique academic years from the documents to the list
                            for (document in documents) {
                                val academicPojo = document.toAcademicPojo()
                                if (!academicYearList.contains(academicPojo.year)) {
                                    academicYearList.add(academicPojo.year)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                try {
                                    // Set up the adapter for the academic year dropdown menu
                                    academicYearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearList)
                                    actAcademicYear.setAdapter(academicYearAdapter)
                                } catch (e: Exception) {
                                    // Log and handle any exceptions that occur during UI update
                                    Log.e(TAG, e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur during data retrieval
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Log and handle any exceptions that occur
            Log.e(TAG, e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }



    /**
     * Loads academic type dropdown menu with available academic types for the selected academic year.
     */
    fun loadAcademicTypeDropDown() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the academic type dropdown menu initially
                    tlAcademicType.isEnabled = false

                    // Clear the existing academic type list
                    academicTypeList.clear()

                    // Launch a coroutine to fetch academic type data in the background
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val academicEvenPojo = AcademicRepository.getAcademicPojoByYearAndType(selectedAcademicYearItem!!,AcademicType.EVEN.name)
                            val academicOddPojo = AcademicRepository.getAcademicPojoByYearAndType(selectedAcademicYearItem!!,AcademicType.ODD.name)
                            if (academicEvenPojo!=null) {
                                academicTypeList.add(AcademicType.EVEN.name)
                            }

                            if (academicOddPojo!=null) {
                                academicTypeList.add(AcademicType.ODD.name)
                            }

                            // Set up the adapter for the academic type dropdown menu
                            academicTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeList)
                            actAcademicType.setAdapter(academicTypeAdapter)

                            // Enable the academic type dropdown menu
                            tlAcademicType.isEnabled = true
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur during data retrieval
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }

                    // Wait for the coroutine job to complete
                    MainScope().launch {
                        job.join()
                    }
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Loads semester dropdown menu with available semesters for the selected academic year and type.
     */
    fun loadSemesterDropDown() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the semester dropdown menu initially
                    tlAcademicSem.isEnabled = false
                    // Launch a coroutine to fetch semester data in the background
                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear the existing semester list
                            semesterList.clear()


                            val academicId:String? = AcademicRepository.getAcademicIdByYearAndType(selectedAcademicYearItem!!,selectedAcademicTypeItem!!)

                            // Retrieve semester documents from the repository
                            val documents = SemesterRepository.getAllSemesterDocuments(academicId!!)

                            // Add semesters from the documents to the list
                            for (document in documents) {
                                semesterList.add(document.toSemesterPojo().name)
                            }

                            // Update the UI on the main thread
                            withContext(Dispatchers.Main) {
                                try {
                                    // Set up the adapter for the semester dropdown menu
                                    semesterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, semesterList)
                                    actAcademicSem.setAdapter(semesterAdapter)

                                    // Enable the semester dropdown menu
                                    tlAcademicSem.isEnabled = true
                                } catch (e: Exception) {
                                    // Log and handle any exceptions that occur during UI update
                                    Log.e(TAG, e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur during data retrieval
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    /**
     * Loads class dropdown menu with available classes for the selected academic year, type, and semester.
     */
    fun loadClassDropDown() {
        binding.apply {
            viewModel.apply {
                try {
                    // Disable the class dropdown menu initially
                    classTIL.isEnabled = false
                    // Launch a coroutine to fetch class data in the background
                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear the existing class list
                            classList.clear()

                            val academicId:String? = AcademicRepository.getAcademicIdByYearAndType(selectedAcademicYearItem!!,selectedAcademicTypeItem!!)

                            val semId:String? = SemesterRepository.getSemesterIdByName(academicId!!, selectedSemesterItem!!)

                            // Retrieve class documents from the repository
                            val documents = ClassRepository.getAllClassDocuments(academicId, semId!!)

                            // Add classes from the documents to the list
                            for (document in documents) {
                                classList.add(document.toClassPojo().name)
                            }

                            // Update the UI on the main thread
                            withContext(Dispatchers.Main) {
                                try {
                                    // Set up the adapter for the class dropdown menu
                                    classAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, classList)
                                    classACTV.setAdapter(classAdapter)

                                    // Enable the class dropdown menu
                                    classTIL.isEnabled = true
                                } catch (e: Exception) {
                                    // Log and handle any exceptions that occur during UI update
                                    Log.e(TAG, e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur during data retrieval
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

}