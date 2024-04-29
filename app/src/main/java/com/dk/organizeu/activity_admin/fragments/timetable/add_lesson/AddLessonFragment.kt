package com.dk.organizeu.activity_admin.fragments.timetable.add_lesson

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
import com.dk.organizeu.activity_admin.dialog.AddLessonDialog
import com.dk.organizeu.adapter.LessonAdapterAdmin
import com.dk.organizeu.databinding.FragmentAddLessonBinding
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.LessonRepository.Companion.lessonDocumentToLessonObj
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.DialogUtils
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.properties.Delegates

class AddLessonFragment : Fragment(),AddLessonDialog.LessonListener, OnItemClickListener {

    companion object {
        lateinit var academicYear:String
        lateinit var semesterNumber:String
        lateinit var academicType:String
        lateinit var className:String
        var selectedTab:Int=0
        fun newInstance() = AddLessonFragment()
        const val TAG = "OrganizeU-AddLessonFragment"
    }

    private lateinit var viewModel: AddLessonViewModel
    private lateinit var binding: FragmentAddLessonBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore
    // Variable to store the day of the week, initialized later
    var dayOfWeek by Delegates.notNull<Int>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_lesson, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[AddLessonViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    // Retrieve arguments from the fragment's bundle
                    requireArguments().apply {
                        AddLessonFragment.apply {
                            // Assign values to fragment properties from arguments
                            academicYear = getString("academic_year", null)
                            academicType = getString("academic_type", null)
                            semesterNumber = getString("academic_semester", null)
                            className = getString("academic_class", null)
                        }
                    }

                    // Calculate the day of the week (1 for Monday, 2 for Tuesday, ..., 7 for Sunday)
                    dayOfWeek = if (UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                        7
                    } else {
                        UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1
                    }
                    selectedTab = dayOfWeek
                    // Load tabs based on the selected day of the week
                    loadTabs()

                    // Initialize the RecyclerView
                    initRecyclerView()

                    // Initialize lessons for the selected day of the week
                    initLesson(dayOfWeek)
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }


                // Set a refresh listener for the swipeRefreshLayout
                swipeRefresh.setOnRefreshListener {
                    // Refresh the Lesson RecyclerView
                    initRecyclerView()

                    // Refresh lessons for the currently selected tab
                    // Note: Assuming selectedTab is 0-based index, so adding 1 to match day of the week (1 for Monday, 2 for Tuesday, ..., 7 for Sunday)
                    initLesson(selectedTab )

                    // Hide the swipe refresh indicator after refreshing
                    swipeRefresh.isRefreshing = false
                }


                btnAddLesson.setOnClickListener {
                    // Try to create and show the AddLessonDialog fragment
                    try {
                        // Create an instance of the AddLessonDialog
                        val dialogFragment = AddLessonDialog(this@AddLessonFragment)

                        // Show the dialog using childFragmentManager
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        // Log and handle any exceptions that occur while showing the dialog
                        Log.e(TAG, e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                // Add a listener to the TabLayout for tab selection events
                tbLayoutWeekDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        // When a tab is selected
                        try {
                            // Update the selectedTab variable with the position of the selected tab
                            selectedTab = tab.position+1

                            // Initialize lessons for the selected day of the week
                            // Note: Assuming selectedTab is 0-based index, so adding 1 to match day of the week (1 for Monday, 2 for Tuesday, ..., 7 for Sunday)
                            initLesson(selectedTab)

                            // Optionally, you can handle different actions based on the selected tab position
                            /*when (tab.position) {
                                0 -> { // Handle actions for the first tab (e.g., Monday)
                                    // Your logic here
                                }
                                1 -> { // Handle actions for the second tab (e.g., Tuesday)
                                    // Your logic here
                                }
                                // Repeat for other tabs as needed
                            }*/
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }
                })

            }
        }
    }

    /**
     * Loads tabs for each day of the week into the TabLayout.
     */
    private fun loadTabs() {
        binding.apply {
            viewModel.apply {
                try {
                    var currentTab: TabLayout.Tab?

                    // Add tabs for each day of the week
                    for (i in 0..6) {
                        // Create a new tab with the name of the weekday
                        currentTab = tbLayoutWeekDay.newTab().setText(UtilFunction.getDayOfWeek(i))

                        // Add the tab to the TabLayout
                        tbLayoutWeekDay.addTab(currentTab)
                    }

                    // Select the tab corresponding to the current day of the week
                    currentTab = tbLayoutWeekDay.getTabAt(dayOfWeek - 1)
                    tbLayoutWeekDay.selectTab(currentTab)
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Initializes the Lesson RecyclerView for displaying lesson data.
     */
    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Show progress bar while initializing Lesson RecyclerView
                    showProgressBar(rvLesson, progressBar)

                    // Clear existing data
                    timetableData.clear()

                    // Set up Lesson RecyclerView layout manager
                    rvLesson.layoutManager = LinearLayoutManager(requireContext())

                    // Initialize the adapter with empty timetableData list
                    lessonAdapter = LessonAdapterAdmin(timetableData,this@AddLessonFragment)

                    // Set the adapter to Lesson RecyclerView
                    rvLesson.adapter = lessonAdapter

                    // Hide progress bar after Lesson RecyclerView setup
                    hideProgressBar(rvLesson, progressBar)
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }



    /**
     * Initializes the lesson data for the specified weekday and updates the Lesson RecyclerView.
     *
     * @param weekDay The day of the week for which lessons are to be initialized.
     */
    private fun initLesson(weekDay: Int) {
        binding.apply {
            viewModel.apply {
                try {
                    MainScope().launch(Dispatchers.IO) {
                        try {
                            // Clear existing timetable data
                            timetableData.clear()

                            // Construct document IDs for fetching lessons
                            val academicDocumentId = "${academicYear}_${academicType}"
                            val semesterDocumentId = semesterNumber
                            val classDocumentId = className
                            val timetableDocumentId = Weekday.getWeekdayNameByNumber(weekDay)

                            // Retrieve lesson documents from the repository
                            val documents = LessonRepository.getAllLessonDocuments(
                                academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId,
                                WeekdayCollection.START_TIME.displayName
                            )

                            var counter = 1
                            // Convert lesson documents to Lesson objects and add them to timetableData
                            for (document in documents) {
                                val lessonItem = lessonDocumentToLessonObj(document, counter)
                                counter++
                                timetableData.add(lessonItem)
                            }

                            withContext(Dispatchers.Main) {
                                try {
                                    // Initialize the adapter with the updated timetableData and set it to Lesson RecyclerView
                                    lessonAdapter = LessonAdapterAdmin(timetableData,this@AddLessonFragment)
                                    rvLesson.adapter = lessonAdapter
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
     * Callback function triggered when a lesson is successfully added.
     * This function is responsible for updating the lesson data for the currently selected day of the week.
     */
    override fun onAddLesson() {
        try {
            // Reinitialize lesson data for the currently selected day of the week
            initLesson(selectedTab)
            requireContext().showToast("Lesson Added Successfully")
        } catch (e: Exception) {
            // Log and handle any exceptions that occur
            Log.e(TAG, e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }

    /**
     * Callback function triggered when there is a conflict while adding a lesson.
     * This function is responsible for notifying the user about the conflict.
     */
    override fun onConflict() {
        // Show a toast message indicating that the lesson already exists
        MainScope().launch(Dispatchers.Main) {
            requireContext().showToast("Lesson is Exists")
        }
    }

    override fun onClick(position: Int) {
    }

    override fun onDeleteClick(position: Int) {
        val dialog = DialogUtils(requireContext()).build()

        dialog.setTitle("Delete Lesson")
            .setCancelable(false)
            .setMessage("Are you sure you want to delete the Lesson and its data?")
            .show({
                // Call the Cloud Function to initiate delete operation
                try {
                    // Get the room document ID at the specified position from the lesson list
                    val academicDocumentId = "${academicYear}_${academicType}"
                    val semesterDocumentId = semesterNumber
                    val classDocumentId = className
                    val timetableDocumentId = Weekday.getWeekdayNameByNumber(selectedTab)
                    val lesson = viewModel.timetableData[position]
                    deleteLesson(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,lesson.id){
                        try {
                            if(it)
                            {
                                viewModel.timetableData.removeAt(position)
                                viewModel.lessonAdapter.notifyItemRemoved(position)
                                requireContext().showToast("Lesson deleted successfully.")
                            }
                            else{
                                requireContext().showToast("Error occur while deleting lesson.")
                            }
                        } catch (e: Exception) {
                            throw e
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG,e.toString())
                    requireContext().showToast("Error occur while deleting lesson.")
                }
                dialog.dismiss()
            },{
                dialog.dismiss()
            })

    }

    override fun onEditClick(position: Int) {
        requireContext().showToast("!Implement Soon!")
    }

    fun deleteLesson(
        academicDocumentId:String,
        semesterDocumentId:String,
        classDocumentId:String,
        timetableDocumentId:String,
        lessonDocumentId:String,
        isDeleted:(Boolean) -> Unit
    ){
        try {
            MainScope().launch(Dispatchers.IO)
            {
                try {
                    LessonRepository.deleteLessonDocument(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId, lessonDocumentId)
                    LessonRepository.isLessonDocumentExists(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,lessonDocumentId){
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