package com.dk.organizeu.student_activity.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.adapter.LessonAdapter
import com.dk.organizeu.databinding.FragmentHomeBinding
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.repository.BatchRepository
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.TimeTableRepository
import com.dk.organizeu.student_activity.StudentActivity
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.LessonReminderReceiver
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.timeFormat
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                scheduleLessonAlarm(requireContext(),"11:58 PM",LessonReminderReceiver.ACTION_START_LESSON,0,Calendar.MONDAY)
                //editLessonAlarm(requireContext(),"11:34 PM",LessonReminderReceiver.ACTION_START_LESSON,0)
                scheduleLessonAlarm(requireContext(),"11:59 PM",LessonReminderReceiver.ACTION_END_LESSON,1,Calendar.MONDAY)
                //cancelLessonAlarm(requireContext(),LessonReminderReceiver.ACTION_END_LESSON,1)
                MainScope().launch(Dispatchers.Main) {
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
                        showProgressBar()
                    }

                    val academicDocumentId = "2024-2025_EVEN"
                    val semesterDocumentId = "2"
                    val classDocumentId = "CEIT-B"

                    loadTimeTableData(academicDocumentId, semesterDocumentId, classDocumentId)
                }

                tbLayoutAction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewModel.selectedTab = tab.position
                        when (tab.position) {
                            0 -> {
                                loadCurrentLesson()
                            }
                            1 -> {
                                try {
                                    if(timetableData[dayOfWeek]!=null) {
                                        currentDayTimeTableData.clear()
                                        //timetableAdapter.notifyItemRangeRemoved(0,currentDayTimeTableData.count())
                                        currentDayTimeTableData.addAll(timetableData[dayOfWeek]!!)
                                        lessonAdapter = LessonAdapter(currentDayTimeTableData)
                                        rvLesson.adapter = lessonAdapter
                                        //timetableAdapter.notifyItemRangeInserted(0,currentDayTimeTableData.count())
                                    }
                                }
                                catch (ex:Exception)
                                {
                                    println(ex.message)
                                }
                            }
                        }
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                    override fun onTabReselected(tab: TabLayout.Tab?) {}

                })

                tbLayoutWeekDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
                            if(currentDayTimeTableData.size>0)
                            {
                                currentDayTimeTableData.clear()
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

    fun editLessonAlarm(context: Context, lessonTime: String, action: String, requestCode: Int) {
        // Cancel the existing alarm
        cancelLessonAlarm(context, action, requestCode)

        // Schedule a new alarm with the updated time
        scheduleLessonAlarm(context, lessonTime, action, requestCode)
    }

    fun scheduleLessonAlarm(context: Context, lessonTime: String, action: String, requestCode:Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            this.action = action
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        if (pendingIntent != null) {
            cancelLessonAlarm(context,action,requestCode)
        }

        val newPendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance()
        calendar.time = timeFormat.parse(lessonTime)!!

        // Set calendar to today's date but with the time from lessonTime
        val now = Calendar.getInstance()
        calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

        // If the lesson time is in the past, schedule it for the next day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Set alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, newPendingIntent)
    }

    fun scheduleLessonAlarm(context: Context, lessonTime: String, action: String, requestCode: Int, lessonWeekday: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            this.action = action
        }

        // Check if the alarm already exists
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        if (pendingIntent != null) {
            cancelLessonAlarm(context, action, requestCode)
        }

        // Create a new PendingIntent
        val newPendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance()
        calendar.time = timeFormat.parse(lessonTime)!!

        // Set the time of the lesson
        val lessonHour = calendar.get(Calendar.HOUR_OF_DAY)
        val lessonMinute = calendar.get(Calendar.MINUTE)

        // Calculate the next occurrence of the lesson based on the current day of the week and the specified lesson weekday
        val today = Calendar.getInstance()
        val daysUntilNextLesson = (lessonWeekday - today.get(Calendar.DAY_OF_WEEK) + 7) % 7
        val nextLessonDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, daysUntilNextLesson)
            set(Calendar.HOUR_OF_DAY, lessonHour)
            set(Calendar.MINUTE, lessonMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Set the alarm to repeat every week
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextLessonDate.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            newPendingIntent
        )
    }


    fun cancelLessonAlarm(context: Context, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun showProgressBar()
    {
        binding.apply {
            rvLesson.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }
    }


    fun hideProgressBar()
    {
        binding.apply {
            progressBar.visibility = View.GONE
            rvLesson.visibility = View.VISIBLE
        }
    }

    private suspend fun loadTimeTableData(academicDocumentId:String,semesterDocumentId:String,classDocumentId:String)
    {
        binding.apply {
            viewModel.apply {
                val timetableList: ArrayList<TimetablePojo> = ArrayList()
                val batchDocuments = BatchRepository.getAllBatchDocuments(academicDocumentId, semesterDocumentId, classDocumentId)
                for(d in batchDocuments)
                {

                }
                val timetableDocuments = TimeTableRepository.getAllTimeTableDocuments(academicDocumentId,semesterDocumentId, classDocumentId)
                for(timetableDocument in timetableDocuments)
                {
                    val lessonDocuments = LessonRepository.getAllLessonDocuments(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocument.id,"start_time")
                    var count = 1
                    for(lessonDocument in lessonDocuments)
                    {
                        timetableList.add(LessonRepository.lessonDocumentToLessonObj(lessonDocument,count++))
                    }
                    timetableData[Weekday.getWeekdayNumberByName(timetableDocument.id)] = ArrayList(timetableList)
                }
                withContext(Dispatchers.Main)
                {
                    loadTabs()
                    initLessonRecyclerView()
                    delay(500)
                    hideProgressBar()
                }
            }
        }
    }

    private fun loadTimeTable(position: Int)
    {
        binding.apply {
            viewModel.apply {
                if(currentDayTimeTableData.size==0)
                {
                    if(timetableData[position]!=null)
                    {
                        currentDayTimeTableData.addAll(timetableData[position]!!)
                        lessonAdapter = LessonAdapter(currentDayTimeTableData)
                        rvLesson.adapter = lessonAdapter
                    }
                }
                else{
                    lessonAdapter.notifyItemRangeRemoved(0,currentDayTimeTableData.count())
                    currentDayTimeTableData.clear()
                    if(timetableData[position]!=null)
                    {
                        currentDayTimeTableData.addAll(timetableData[position]!!)
                        lessonAdapter.notifyItemRangeInserted(0,currentDayTimeTableData.count())
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

            }
        }
    }

    private fun loadCurrentLesson()
    {
        binding.apply {
            viewModel.apply {
                var len = currentDayTimeTableData.size
                var i = 0
                while (i<len)
                {
                    if (!UtilFunction.checkLessonStatus(
                            currentDayTimeTableData[i].startTime,
                            currentDayTimeTableData[i].endTime
                        )
                    )
                    {
                        currentDayTimeTableData.removeAt(i)
                        lessonAdapter.notifyItemRemoved(i)
                        len--
                        i--
                    }
                    i++
                }
            }
        }
    }

    private fun initLessonRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                rvLesson.layoutManager = LinearLayoutManager(requireContext())
                currentDayTimeTableData.clear()
                if(timetableData[selectedWeekDayTab+1]!=null)
                {
                    currentDayTimeTableData.addAll(timetableData[selectedWeekDayTab+1]!!)
                }
                else{
                    currentDayTimeTableData.addAll(ArrayList())
                }
                lessonAdapter = LessonAdapter(currentDayTimeTableData)
                if(tbLayoutAction.selectedTabPosition==0 && selectedTab==0 && tbLayoutAction.isVisible)
                {
                    loadCurrentLesson()
                }
                rvLesson.adapter = lessonAdapter
            }
        }
    }

    private fun loadTabs()
    {
        binding.apply {
            viewModel.apply {
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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? StudentActivity)?.drawerMenuSelect(R.id.nav_home)
    }
}