package com.dk.organizeu.student_activity.fragments

import android.os.Binder
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentHomeBinding
import com.dk.organizeu.student_activity.StudentActivity
import com.dk.organizeu.student_activity.adapter.TimetableAdapter
import com.dk.organizeu.student_activity.data_class.TimetableItem
import com.dk.organizeu.utils.UtilFunction
import com.google.android.material.tabs.TabLayout
import java.util.*

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                dayOfWeek = if(UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                    7
                } else{
                    UtilFunction.calendar.get(Calendar.DAY_OF_WEEK) - 1
                }

                if (savedInstanceState == null){
                    selectedWeekDayTab = dayOfWeek-1
                    println(selectedWeekDayTab)
                }


                loadTimeTableData()

                loadTabs()

                if (savedInstanceState != null) {
                    rvLesson.layoutManager = LinearLayoutManager(requireContext())
                    rvLesson.adapter = timetableAdapter
                }
                else{
                    initLessonRecyclerView()
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
                                    if(timetableData[dayOfWeek]!=null)
                                    {
                                        if(currentDayTimeTableData.size!=0)
                                        {
                                            currentDayTimeTableData.clear()
                                        }
                                        //timetableAdapter.notifyItemRangeRemoved(0,currentDayTimeTableData.count())
                                        currentDayTimeTableData.addAll(timetableData[dayOfWeek]!!)
                                        timetableAdapter = TimetableAdapter(currentDayTimeTableData)
                                        rvLesson.adapter = timetableAdapter
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
                        loadTimeTable(tab.position+1)
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

    private fun loadTimeTableData()
    {
        binding.apply {
            viewModel.apply {

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
                        timetableAdapter = TimetableAdapter(currentDayTimeTableData)
                        rvLesson.adapter = timetableAdapter
                    }
                }
                else{
                    timetableAdapter.notifyItemRangeRemoved(0,currentDayTimeTableData.count())
                    currentDayTimeTableData.clear()
                    if(timetableData[position]!=null)
                    {
                        currentDayTimeTableData.addAll(timetableData[position]!!)
                        timetableAdapter.notifyItemRangeInserted(0,currentDayTimeTableData.count())
                    }
                }

                if(dayOfWeek==position)
                {
                    tbLayoutAction.visibility= View.VISIBLE
                    if(tbLayoutAction.getTabAt(0)!!.isSelected)
                    {
                        loadCurrentLesson()
                    }
                }
                else{
                    tbLayoutAction.visibility= View.GONE
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
                        timetableAdapter.notifyItemRemoved(i)
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
                    currentDayTimeTableData.addAll(ArrayList<TimetableItem>())
                }
                timetableAdapter = TimetableAdapter(currentDayTimeTableData)
                if(tbLayoutAction.selectedTabPosition==0 && selectedTab==0)
                {
                    loadCurrentLesson()
                }
                rvLesson.adapter = timetableAdapter
            }
        }
    }

    private fun loadTabs()
    {
        binding.apply {
            viewModel.apply {
                var currentTab: TabLayout.Tab?

                for(i in 0 .. 6)
                {
                    currentTab = tbLayoutWeekDay.newTab().setText(UtilFunction.getDayOfWeek(i))
                    tbLayoutWeekDay.addTab(currentTab)
                    if(selectedWeekDayTab==i)
                    {
                        tbLayoutWeekDay.selectTab(currentTab)
                    }
                }
                currentTab = tbLayoutAction.newTab().setText("Current")
                tbLayoutAction.addTab(currentTab)
                currentTab = tbLayoutAction.newTab().setText("  All  ")
                tbLayoutAction.addTab(currentTab)
                currentTab = tbLayoutAction.getTabAt(selectedTab)
                tbLayoutAction.selectTab(currentTab)

                if(dayOfWeek-1 == tbLayoutWeekDay.selectedTabPosition)
                {
                    tbLayoutAction.visibility= View.VISIBLE
                }
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