package com.dk.organizeu.activity_student.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.adapter.LessonAdapterStudent
import com.dk.organizeu.databinding.FragmentHomeBinding
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.pojo.LessonPojo
import com.dk.organizeu.pojo.LessonPojo.Companion.toLessonPojo
import com.dk.organizeu.pojo.TimetablePojo.Companion.toTimetablePojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.repository.TimeTableRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.LessonMuteManagement
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.dk.organizeu.utils.Validation.Companion.checkLessonStatus
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
        const val TAG = "OrganizeU-HomeFragment"
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var lessonMuteManagement: LessonMuteManagement

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        lessonMuteManagement = LessonMuteManagement()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {

                MainScope().launch(Dispatchers.Main) {
                    try {
                        // Determine the current day of the week
                        dayOfWeek = if(UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                            7
                        } else {
                            UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1
                        }

                        // Set the selectedWeekDayTab to the current day of the week if savedInstanceState is null
                        if (savedInstanceState == null){
                            selectedWeekDayTab = dayOfWeek-1
                            println(selectedWeekDayTab)
                        }

                        // Load tabs and show progress bar
                        withContext(Dispatchers.Main)
                        {
                            loadTabs()
                            showProgressBar(rvLesson,progressBar)
                        }



                        val academicId: String? = AcademicRepository.getAcademicIdByYearAndType(SharedPreferencesManager.getString(requireContext(),StudentLocalDBKey.ACADEMIC_YEAR.displayName), SharedPreferencesManager.getString(requireContext(),StudentLocalDBKey.ACADEMIC_TYPE.displayName))

                        val semId:String? = SemesterRepository.getSemesterIdByName(academicId!!, SharedPreferencesManager.getString(requireContext(),StudentLocalDBKey.SEMESTER.displayName))

                        val classId:String? = ClassRepository.getClassIdByName(academicId,semId!!, SharedPreferencesManager.getString(requireContext(),StudentLocalDBKey.CLASS.displayName))

                        // Load timetable data based on academic, semester, and class IDs
                        loadTimeTableData(academicId, semId, classId!!)

                        // Set refresh listener for swipeRefreshLayout
                        swipeRefresh.setOnRefreshListener {
                            MainScope().launch(Dispatchers.Main)
                            {
                                // Load timetable data based on academic, semester, and class IDs
                                loadTimeTableData(academicId, semId, classId)
                                // Load timetable for the selected week day tab
                                loadTimeTable(selectedWeekDayTab+1)
                                // Stop the refreshing animation
                                swipeRefresh.isRefreshing=false
                            }
                        }
                    } catch (e: Exception) {
                        // Handle any exceptions and log error messages
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }


                // Add a listener to the TabLayout for tab selection events
                tbLayoutAction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        try {
                            // Update the selected tab position in the view model
                            viewModel.selectedTab = tab.position
                            // Check which tab is selected
                            when (tab.position) {
                                0 -> {
                                    // Load current lesson for the selected tab
                                    loadCurrentLesson()
                                }
                                1 -> {
                                    // Load the timetable data for the current day
                                    try {
                                        // Check if timetable data exists for the current day
                                        if (timetableData[dayOfWeek] != null) {
                                            // Clear the current day's timetable data
                                            currentDayTimeTableData.value!!.clear()
                                            // Add all timetable data for the current day to the currentDayTimeTableData LiveData
                                            currentDayTimeTableData.value!!.addAll(timetableData[dayOfWeek]!!)
                                            // Update the value of currentDayTimeTableData to trigger observers
                                            currentDayTimeTableData.value = currentDayTimeTableData.value
                                            // Create a new LessonAdapter with the updated timetable data
                                            lessonAdapter = LessonAdapterStudent(currentDayTimeTableData.value!!)
                                            // Set the adapter for the RecyclerView to display the updated timetable data
                                            rvLesson.adapter = lessonAdapter
                                        }
                                    } catch (e: Exception) {
                                        // Handle any exceptions that occur during the loading of timetable data
                                        Log.e(TAG,e.message.toString())
                                        requireContext().unexpectedErrorMessagePrint(e)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Handle any exceptions that occur during the tab selection process
                            Log.e(TAG,e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })


                // Add a listener to the TabLayout for tab selection events
                tbLayoutWeekDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        // Set the selectedWeekDayTab variable in the view model to the selected tab position
                        viewModel.selectedWeekDayTab = tab.position
                        try {
                            // Check if the selected tab corresponds to the current day of the week
                            if (dayOfWeek == tab.position + 1) {
                                // If yes, make the action tab layout visible
                                tbLayoutAction.visibility = View.VISIBLE
                            } else {
                                // If not, hide the action tab layout
                                tbLayoutAction.visibility = View.GONE
                            }
                            // Check if timetable data is available for the selected tab position
                            if (timetableData[tab.position + 1]!!.isNotEmpty()) {
                                // If yes, load the timetable for the selected day
                                loadTimeTable(tab.position + 1)
                            }
                        } catch (e: Exception) {
                            // If an exception occurs while loading the timetable data, clear the current day's timetable data
                            if (currentDayTimeTableData.value!!.size > 0) {
                                currentDayTimeTableData.value!!.clear()
                                lessonAdapter.notifyDataSetChanged()
                            }
                        }

                        /* Uncomment the following code block if you want to load timetable data based on tab position
                        when (tab.position) {
                            0 -> {
                                loadTimeTable(1)
                            }
                            1 -> {
                                loadTimeTable(2)
                            }
                            2 -> {
                                loadTimeTable(3)
                            }
                            3 -> {
                                loadTimeTable(4)
                            }
                            4 -> {
                                loadTimeTable(5)
                            }
                            5 -> {
                                loadTimeTable(6)
                            }
                        }
                        */
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
     * Asynchronously loads timetable data for a specific academic year, semester, and class.
     * @param academicDocumentId The ID of the academic document.
     * @param semesterDocumentId The ID of the semester document.
     * @param classDocumentId The ID of the class document.
     */
    private suspend fun loadTimeTableData(academicDocumentId: String, semesterDocumentId: String, classDocumentId: String) {
        binding.apply {
            viewModel.apply {
                try {
                    // Initialize an empty list to store timetable data
                    val timetableList: ArrayList<LessonPojo> = ArrayList()

                    // Retrieve all timetable documents for the specified academic year, semester, and class
                    val timetableDocuments = TimeTableRepository.getAllTimeTableDocuments(academicDocumentId, semesterDocumentId, classDocumentId)

                    // Iterate through each timetable document
                    for (timetableDocument in timetableDocuments) {
                        // Clear the timetable list for each day
                        timetableList.clear()
                        val timetablePojo = timetableDocument.toTimetablePojo()
                        // Get the weekday number from the timetable document's ID
                        val weekDayNumber = Weekday.getWeekdayNumberByName(timetablePojo.name)

                        // Retrieve all lesson documents for the current timetable document
                        val lessonDocuments = LessonRepository.getAllLessonDocuments(academicDocumentId, semesterDocumentId, classDocumentId, timetablePojo.id, "startTime")

                        // Iterate through each lesson document
                        for (lessonDocument in lessonDocuments) {
                            // Convert the lesson document to a TimetablePojo object
                            val lesson = lessonDocument.toLessonPojo()
                            timetableList.add(lesson)


                            // lessonMuteManagement.scheduleLessonAlarm(requireContext(), lesson.startTime, ACTION_START_LESSON, lesson.muteRequestCode, Weekday.getSystemWeekDayByNumber(weekDayNumber))
                            // lessonMuteManagement.scheduleLessonAlarm(requireContext(), lesson.endTime, ACTION_END_LESSON, lesson.unmuteRequestCode, Weekday.getSystemWeekDayByNumber(weekDayNumber))
                        }

                        // Add the timetable data for the current day to the timetableData map
                        timetableData[weekDayNumber] = ArrayList(timetableList)
                    }

                    // Update the UI on the main thread
                    withContext(Dispatchers.Main) {
                        try {
                            // Initialize the lesson recycler view and hide the progress bar
                            initLessonRecyclerView()
                            delay(500)
                            hideProgressBar(rvLesson, progressBar)
                        } catch (e: Exception) {
                            Log.e(TAG, e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Loads the timetable data for the specified position (day) and updates the UI accordingly.
     * @param position The position (day) for which the timetable data should be loaded.
     */
    private fun loadTimeTable(position: Int) {
        binding.apply {
            viewModel.apply {
                try {
                    // Check if the current day timetable data is empty
                    if (currentDayTimeTableData.value!!.isEmpty()) {
                        // If the timetable data for the position is not null, add it to the current day timetable data
                        if (timetableData[position] != null) {
                            currentDayTimeTableData.value!!.addAll(timetableData[position]!!)
                            // Initialize the lesson adapter and set it to the recycler view
                            lessonAdapter = LessonAdapterStudent(currentDayTimeTableData.value!!)
                            rvLesson.adapter = lessonAdapter
                        }
                    } else {
                        // If the current day timetable data is not empty, clear it and update the adapter
                        lessonAdapter.notifyItemRangeRemoved(0, currentDayTimeTableData.value!!.size)
                        currentDayTimeTableData.value!!.clear()
                        // If the timetable data for the position is not null, add it to the current day timetable data
                        if (timetableData[position] != null) {
                            currentDayTimeTableData.value!!.addAll(timetableData[position]!!)
                            // Notify the adapter about the data changes
                            lessonAdapter.notifyItemRangeInserted(0, currentDayTimeTableData.value!!.size)
                        }
                    }

                    // Check if the action tab layout is visible
                    if (tbLayoutAction.isVisible) {
                        // Get the current selected tab
                        val currentTab = tbLayoutAction.getTabAt(0)!!
                        if (currentTab.isSelected) {
                            // If the first tab is selected, load the current lesson
                            loadCurrentLesson()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Removes the lessons from the current day timetable data that have already ended.
     * This function checks the end time of each lesson and removes it if the lesson has ended.
     * It updates the adapter to reflect the changes in the UI.
     */
    private fun loadCurrentLesson() {
        binding.apply {
            viewModel.apply {
                try {
                    var len = currentDayTimeTableData.value!!.size
                    var i = 0
                    // Iterate through each lesson in the current day timetable data
                    while (i < len) {
                        // Check if the lesson has already ended
                        if (!checkLessonStatus(currentDayTimeTableData.value!![i].endTime)) {
                            // If the lesson has ended, remove it from the timetable data and update the adapter
                            currentDayTimeTableData.value!!.removeAt(i)
                            lessonAdapter.notifyItemRemoved(i)
                            lessonAdapter.notifyItemRangeChanged(i,lessonAdapter.itemCount-i)
                            len--
                            i--
                        }
                        i++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    /**
     * Initializes the lesson recycler view by setting up its layout manager,
     * populating it with lesson data for the selected weekday, and setting up the adapter.
     * If the action tab is selected and it's the first tab, it loads the current lesson.
     */
    private fun initLessonRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    // Set up the layout manager for the recycler view
                    rvLesson.layoutManager = LinearLayoutManager(requireContext())

                    // Clear the current day's timetable data
                    currentDayTimeTableData.value!!.clear()

                    // Populate the current day's timetable data with the lessons for the selected weekday
                    if (timetableData[selectedWeekDayTab + 1] != null) {
                        currentDayTimeTableData.value!!.addAll(timetableData[selectedWeekDayTab + 1]!!)
                    } else {
                        currentDayTimeTableData.value!!.addAll(ArrayList())
                    }

                    // Set up the adapter for the recycler view
                    lessonAdapter = LessonAdapterStudent(currentDayTimeTableData.value!!)

                    // If the action tab is selected, it's the first tab, and it's visible, load the current lesson
                    if (tbLayoutAction.selectedTabPosition == 0 && selectedTab == 0 && tbLayoutAction.isVisible) {
                        loadCurrentLesson()
                    }

                    rvLesson.adapter = lessonAdapter
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    private fun loadTabs()
    {
        binding.apply {
            viewModel.apply {
                try {
                    var currentTab: TabLayout.Tab?

                    // Add tab of Weekday view
                    for(i in 0 .. 6)
                    {
                        currentTab = tbLayoutWeekDay.newTab().setText(UtilFunction.getDayOfWeek(i))
                        tbLayoutWeekDay.addTab(currentTab)
                    }

                    // Add Tab of Action
                    currentTab = tbLayoutAction.newTab().setText("Current")
                    tbLayoutAction.addTab(currentTab)
                    currentTab = tbLayoutAction.newTab().setText("  All  ")
                    tbLayoutAction.addTab(currentTab)

                    // fetch Default selection tab and select
                    currentTab = tbLayoutAction.getTabAt(selectedTab)
                    tbLayoutAction.selectTab(currentTab)

                    // Select Weekday tab based of current week day
                    currentTab = tbLayoutWeekDay.getTabAt(dayOfWeek-1)
                    tbLayoutWeekDay.selectTab(currentTab)


                    // Check Whether the Action tag show or not
                    // Show if the selected weekday tab is current weekday
                    if(dayOfWeek-1 == tbLayoutWeekDay.selectedTabPosition)
                    {
                        tbLayoutAction.visibility= View.VISIBLE
                    }
                    // invisible if selected weekday tab is not current weekday
                    else{
                        tbLayoutAction.visibility= View.GONE
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            // Select the home item in the navigation drawer
            (activity as? StudentActivity)?.drawerMenuSelect(R.id.nav_home)

            // Enable the back button in the action bar
            (activity as? StudentActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // Set the home button indicator to the menu icon
            (activity as? StudentActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }
}