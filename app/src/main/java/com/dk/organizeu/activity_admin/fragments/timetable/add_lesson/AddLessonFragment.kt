package com.dk.organizeu.activity_admin.fragments.timetable.add_lesson

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.dialog.AddLessonDialog
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.databinding.FragmentAddLessonBinding
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.LessonRepository.Companion.lessonDocumentToLessonObj
import com.dk.organizeu.adapter.LessonAdapterAdmin
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.properties.Delegates

class AddLessonFragment : Fragment(),AddLessonDialog.LessonListener {

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
    var dayOfWeek by Delegates.notNull<Int>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_lesson, container, false)
        binding = FragmentAddLessonBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddLessonViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db = FirebaseFirestore.getInstance()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    requireArguments().apply {
                        AddLessonFragment.apply {
                            academicYear = getString("academic_year",null)
                            academicType = getString("academic_type",null)
                            semesterNumber = getString("academic_semester",null)
                            className = getString("academic_class",null)
                        }
                    }
                    dayOfWeek = if(UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                        7
                    } else {
                        UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1
                    }
                    loadTabs()
                    initRecyclerView()
                    initLesson(dayOfWeek)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

                swipeRefresh.setOnRefreshListener {
                    initRecyclerView()
                    initLesson(selectedTab+1)
                    swipeRefresh.isRefreshing=false
                }
                btnAddLesson.setOnClickListener {
                    try {
                        val dialogFragment = AddLessonDialog(this@AddLessonFragment)
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                tbLayoutWeekDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        try {
                            selectedTab = tab.position
                            initLesson(selectedTab+1)
                            /*when (tab.position) {
                                    0 -> {

                                    }
                                    1 -> {

                                    }
                                    2 -> {

                                    }
                                    3 -> {

                                    }
                                    4 -> {

                                    }
                                    5 -> {

                                    }
                                    6 -> {

                                    }
                                }*/
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
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

                    // Select Weekday tab based of current week day
                    currentTab = tbLayoutWeekDay.getTabAt(dayOfWeek-1)
                    tbLayoutWeekDay.selectTab(currentTab)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                try {
                    showProgressBar(rvLesson,progressBar)
                    timetableData.clear()
                    rvLesson.layoutManager = LinearLayoutManager(requireContext())
                    lessonAdapter = LessonAdapterAdmin(timetableData)
                    rvLesson.adapter = lessonAdapter
                    hideProgressBar(rvLesson,progressBar)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    private fun initLesson(weekDay:Int)
    {
        binding.apply {
            viewModel.apply {
                try {
                    MainScope().launch(Dispatchers.IO){
                        try {
                            timetableData.clear()
                            val academicDocumentId = "${academicYear}_${academicType}"
                            val semesterDocumentId = semesterNumber
                            val classDocumentId = className
                            val timetableDocumentId = Weekday.getWeekdayNameByNumber(weekDay)

                            val documents = LessonRepository.getAllLessonDocuments(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId,
                                WeekdayCollection.START_TIME.displayName)
                            var counter = 1
                            for(document in documents)
                            {
                                val lessonItem = lessonDocumentToLessonObj(document,counter)
                                counter++
                                timetableData.add(lessonItem)
                            }

                            withContext(Dispatchers.Main)
                            {
                                try {
                                    lessonAdapter = LessonAdapterAdmin(timetableData)
                                    rvLesson.adapter = lessonAdapter
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
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onAddLesson() {
        try {
            initLesson(selectedTab+1)
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }

    override fun onConflict() {
        MainScope().launch(Dispatchers.Main)
        {
            Toast.makeText(requireContext(),"Lesson Already Exist", Toast.LENGTH_SHORT).show()
        }
    }
}