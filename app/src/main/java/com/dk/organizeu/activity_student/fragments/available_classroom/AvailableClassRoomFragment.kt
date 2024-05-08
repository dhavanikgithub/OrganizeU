package com.dk.organizeu.activity_student.fragments.available_classroom

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_student.fragments.home.HomeFragment
import com.dk.organizeu.adapter.AvailableClassRoomAdapter
import com.dk.organizeu.databinding.FragmentAvailableClassRoomBinding
import com.dk.organizeu.pojo.AvailableClassRoomPojo
import com.dk.organizeu.pojo.ClassPojo.Companion.toClassPojo
import com.dk.organizeu.pojo.LessonPojo
import com.dk.organizeu.pojo.LessonPojo.Companion.toLessonPojo
import com.dk.organizeu.pojo.RoomPojo
import com.dk.organizeu.pojo.RoomPojo.Companion.toRoomPojo
import com.dk.organizeu.pojo.SemesterPojo.Companion.toSemesterPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.repository.TimeTableRepository
import com.dk.organizeu.utils.TimeConverter.Companion.calculateLessonDuration
import com.dk.organizeu.utils.TimeConverter.Companion.convert24HourTo12Hour
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class AvailableClassRoomFragment : Fragment() {

    private lateinit var viewModel: AvailableClassRoomViewModel
    private lateinit var binding: FragmentAvailableClassRoomBinding
    private var academicId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_available_class_room, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[AvailableClassRoomViewModel::class.java]
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        updateRecyclerView(binding.chipMon.text.toString())

        binding.cgWeekday.setOnCheckedStateChangeListener { chipGroup, ints ->
            val weekday:String
            when(true)
            {
                binding.chipTue.isChecked -> {
                    weekday = binding.chipTue.text.toString()
                }
                binding.chipWed.isChecked -> {
                    weekday = binding.chipWed.text.toString()
                }
                binding.chipThu.isChecked -> {
                    weekday = binding.chipThu.text.toString()
                }
                binding.chipFri.isChecked -> {
                    weekday = binding.chipFri.text.toString()
                }
                binding.chipSat.isChecked -> {
                    weekday = binding.chipSat.text.toString()
                }
                else -> {
                    weekday = "Monday"
                }
            }
            resetRecyclerView()
            updateRecyclerView(weekday)
        }
    }

    private fun initRecyclerView()
    {
        viewModel.availableClassRoomPojos.clear()
        viewModel.availableClassRoomAdapter = AvailableClassRoomAdapter(viewModel.availableClassRoomPojos)
        binding.rvAvailableClassRoom.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableClassRoom.adapter = viewModel.availableClassRoomAdapter
    }

    private fun resetRecyclerView()
    {
        viewModel.availableClassRoomPojos.clear()
        viewModel.availableClassRoomAdapter!!.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateRecyclerView(weekday: String){
        MainScope().launch(Dispatchers.IO)
        {
            try {
                withContext(Dispatchers.Main)
                {
                    showProgressBar(binding.rvAvailableClassRoom,binding.progressBar)
                }
                academicId = AcademicRepository.getAcademicIdByYearAndType("2023-2024", "ODD")
                loadRoomData()
                // Load timetable data based on academic, semester, and class IDs
                loadTimeTableData(academicId!!,weekday)
                withContext(Dispatchers.Main)
                {
                    findAvailableClassrooms(viewModel.roomData,viewModel.lessonData)
                    viewModel.availableClassRoomAdapter!!.notifyDataSetChanged()
                    hideProgressBar(binding.rvAvailableClassRoom,binding.progressBar)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findAvailableClassrooms(classrooms: List<RoomPojo>, lessons: List<LessonPojo>){
        for (classroom in classrooms) {
            // Initialize classroom availability from 9 am to 4 pm
            var startTime = LocalTime.of(9, 0)
            val endTime = LocalTime.of(16, 0)

            // Subtract lesson times from classroom availability
            for (lesson in lessons) {
                var temp = lesson.startTime.split(":")
                val lessonStartTime = LocalTime.of(temp[0].toInt(),temp[1].toInt())
                temp = lesson.endTime.split(":")
                val lessonEndTime = LocalTime.of(temp[0].toInt(),temp[1].toInt())
                if (lesson.location == "${classroom.name} - ${classroom.location}") {
                    // Adjust start and end times based on lesson times
                    if (lessonStartTime > startTime) {
                        val availableClassRoomPojo = AvailableClassRoomPojo(
                            "${classroom.name} - ${classroom.location}",
                            "${startTime.convert24HourTo12Hour()} - ${lessonStartTime.convert24HourTo12Hour()}",
                            calculateLessonDuration(startTime.toString(),lessonStartTime.toString())
                        )
                        viewModel.availableClassRoomPojos.add(availableClassRoomPojo)
                    }
                    startTime = lessonEndTime
                }
            }

            // Add remaining available time slot after the last lesson
            if (endTime > startTime) {
                val availableClassRoomPojo = AvailableClassRoomPojo(
                    "${classroom.name} - ${classroom.location}",
                    "${startTime.convert24HourTo12Hour()} - ${endTime.convert24HourTo12Hour()}",
                    calculateLessonDuration(startTime.toString(),endTime.toString())
                )
                viewModel.availableClassRoomPojos.add(availableClassRoomPojo)
            }
        }

    }



    private suspend fun loadRoomData()
    {
        viewModel.apply {
            roomData.clear()
            val allRoomDocument = RoomRepository.getAllRoomDocument()
            allRoomDocument.map {
                roomData.add(it.toRoomPojo())
            }
        }
    }

    /**
     * Asynchronously loads timetable data for a specific academic year, semester, and class.
     * @param academicDocumentId The ID of the academic document.
     */
    private suspend fun loadTimeTableData(academicDocumentId: String,weekday: String) {
        binding.apply {
            viewModel.apply {
                try {
                    viewModel.lessonData.clear()
                    val allSemesterDocuments = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
                    allSemesterDocuments.map {semesterDoc ->
                        val semPojo = semesterDoc.toSemesterPojo()
                        val allClassDocuments = ClassRepository.getAllClassDocuments(academicDocumentId,semPojo.id)
                        allClassDocuments.map {classDoc ->
                            val classPojo = classDoc.toClassPojo()
                            try {
                                val timeTableId = TimeTableRepository.getTimetableIdByName(academicDocumentId,semPojo.id,classPojo.id,weekday)
                                val allLessonDocuments = LessonRepository.getAllLessonDocuments(academicDocumentId,semPojo.id,classPojo.id, timeTableId!!)
                                allLessonDocuments.map {
                                    viewModel.lessonData.add(it.toLessonPojo())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                catch (e: NullPointerException)
                {
                    e.printStackTrace()
                }
                catch (e: Exception) {
                    Log.e(HomeFragment.TAG, e.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }
}