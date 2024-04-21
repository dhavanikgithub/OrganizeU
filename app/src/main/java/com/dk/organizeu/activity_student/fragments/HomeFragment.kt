package com.dk.organizeu.activity_student.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.adapter.LessonAdapterStudent
import com.dk.organizeu.databinding.FragmentHomeBinding
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.TimeTableRepository
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.LessonMuteManagement
import com.dk.organizeu.utils.NotificationScheduler.Companion.scheduleNotification
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


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
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        lessonMuteManagement = LessonMuteManagement()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                scheduleNotification(requireContext(),"TOC","8:37 AM")
                MainScope().launch(Dispatchers.Main) {
                    try {
                        dayOfWeek = if(UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                            7
                        } else {
                            UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1
                        }

                        if (savedInstanceState == null){
                            selectedWeekDayTab = dayOfWeek-1
                            println(selectedWeekDayTab)
                        }

                        withContext(Dispatchers.Main)
                        {
                            showProgressBar(rvLesson,progressBar)
                        }

                        val academicDocumentId = "2024-2025_EVEN"
                        val semesterDocumentId = "2"
                        val classDocumentId = "CEIT-A"

                        loadTimeTableData(academicDocumentId, semesterDocumentId, classDocumentId)

                        swipeRefresh.setOnRefreshListener {
                            loadTimeTable(selectedWeekDayTab+1)
                            swipeRefresh.isRefreshing=false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                tbLayoutAction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        try {
                            viewModel.selectedTab = tab.position
                            when (tab.position) {
                                0 -> {
                                    loadCurrentLesson()
                                }
                                1 -> {
                                    try {
                                        if(timetableData[dayOfWeek]!=null) {
                                            currentDayTimeTableData.value!!.clear()
                                            //timetableAdapter.notifyItemRangeRemoved(0,currentDayTimeTableData.count())
                                            currentDayTimeTableData.value!!.addAll(timetableData[dayOfWeek]!!)
                                            currentDayTimeTableData.value = currentDayTimeTableData.value
                                            lessonAdapter = LessonAdapterStudent(currentDayTimeTableData.value!!)
                                            rvLesson.adapter = lessonAdapter
                                            //timetableAdapter.notifyItemRangeInserted(0,currentDayTimeTableData.count())
                                        }
                                    }
                                    catch (e:Exception)
                                    {
                                        Log.e(TAG,e.message.toString())
                                        requireContext().unexpectedErrorMessagePrint(e)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                    override fun onTabReselected(tab: TabLayout.Tab?) {}

                })

                tbLayoutWeekDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewModel.selectedWeekDayTab = tab.position
                        try{
                            if(dayOfWeek==tab.position+1)
                            {
                                tbLayoutAction.visibility= View.VISIBLE
                            }
                            else{
                                tbLayoutAction.visibility= View.GONE
                            }
                            if(timetableData[tab.position+1]!!.isNotEmpty())
                            {
                                loadTimeTable(tab.position+1)
                            }
                        }
                        catch (e:Exception){
                            if(currentDayTimeTableData.value!!.size>0)
                            {
                                currentDayTimeTableData.value!!.clear()
                                lessonAdapter.notifyDataSetChanged()
                            }
                        }

                        /*when (tab.position) {
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
                        }*/
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                })
            }
        }
    }


    private suspend fun loadTimeTableData(academicDocumentId:String,semesterDocumentId:String,classDocumentId:String)
    {
        binding.apply {
            viewModel.apply {
                try {
                    val timetableList: ArrayList<TimetablePojo> = ArrayList()
                    val timetableDocuments = TimeTableRepository.getAllTimeTableDocuments(academicDocumentId,semesterDocumentId, classDocumentId)
                    for(timetableDocument in timetableDocuments)
                    {
                        timetableList.clear()
                        val weekDayNumber = Weekday.getWeekdayNumberByName(timetableDocument.id)
                        val lessonDocuments = LessonRepository.getAllLessonDocuments(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocument.id,"start_time")
                        var count = 1
                        var lesson: TimetablePojo
                        for(lessonDocument in lessonDocuments)
                        {
                            lesson = LessonRepository.lessonDocumentToLessonObj(lessonDocument,count++)
                            timetableList.add(lesson)
                            //lessonMuteManagement.scheduleLessonAlarm(requireContext(),lesson.startTime,ACTION_START_LESSON,totalLessonCount++,Weekday.getSystemWeekDayByNumber(weekDayNumber))
                            //lessonMuteManagement.scheduleLessonAlarm(requireContext(),lesson.endTime,ACTION_END_LESSON,totalLessonCount++,Weekday.getSystemWeekDayByNumber(weekDayNumber))
                        }
                        timetableData[weekDayNumber] = ArrayList(timetableList)
                    }
                    withContext(Dispatchers.Main)
                    {
                        try {
                            loadTabs()
                            initLessonRecyclerView()
                            delay(500)
                            hideProgressBar(rvLesson,progressBar)
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun loadTimeTable(position: Int)
    {
        binding.apply {
            viewModel.apply {
                try {
                    if(currentDayTimeTableData.value!!.size==0)
                    {
                        if(timetableData[position]!=null)
                        {
                            currentDayTimeTableData.value!!.addAll(timetableData[position]!!)
                            lessonAdapter = LessonAdapterStudent(currentDayTimeTableData.value!!)
                            rvLesson.adapter = lessonAdapter
                        }
                    }
                    else{
                        lessonAdapter.notifyItemRangeRemoved(0,currentDayTimeTableData.value!!.count())
                        currentDayTimeTableData.value!!.clear()
                        if(timetableData[position]!=null)
                        {
                            currentDayTimeTableData.value!!.addAll(timetableData[position]!!)
                            lessonAdapter.notifyItemRangeInserted(0,currentDayTimeTableData.value!!.count())
                        }
                    }

                    if(tbLayoutAction.isVisible)
                    {
                        val currentTab = tbLayoutAction.getTabAt(0)!!
                        if(currentTab.isSelected)
                        {
                            loadCurrentLesson()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun loadCurrentLesson()
    {
        binding.apply {
            viewModel.apply {
                try {
                    var len = currentDayTimeTableData.value!!.size
                    var i = 0
                    while (i<len)
                    {
                        if (!UtilFunction.checkLessonStatus(
                                currentDayTimeTableData.value!![i].startTime,
                                currentDayTimeTableData.value!![i].endTime
                            )
                        )
                        {
                            currentDayTimeTableData.value!!.removeAt(i)
                            lessonAdapter.notifyItemRemoved(i)
                            len--
                            i--
                        }
                        i++
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun initLessonRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                try {
                    rvLesson.layoutManager = LinearLayoutManager(requireContext())
                    currentDayTimeTableData.value!!.clear()
                    if(timetableData[selectedWeekDayTab+1]!=null)
                    {
                        currentDayTimeTableData.value!!.addAll(timetableData[selectedWeekDayTab+1]!!)
                    }
                    else{
                        currentDayTimeTableData.value!!.addAll(ArrayList())
                    }
                    lessonAdapter = LessonAdapterStudent(currentDayTimeTableData.value!!)
                    if(tbLayoutAction.selectedTabPosition==0 && selectedTab==0 && tbLayoutAction.isVisible)
                    {
                        loadCurrentLesson()
                    }
                    rvLesson.adapter = lessonAdapter
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
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
            (activity as? StudentActivity)?.drawerMenuSelect(R.id.nav_home)
            (activity as? StudentActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as? StudentActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }
}