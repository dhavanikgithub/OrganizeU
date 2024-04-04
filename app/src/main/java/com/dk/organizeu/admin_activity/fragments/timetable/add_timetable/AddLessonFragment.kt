package com.dk.organizeu.admin_activity.fragments.timetable.add_timetable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.dialog_box.AddLessonDialog
import com.dk.organizeu.admin_activity.enum_class.Weekday
import com.dk.organizeu.databinding.FragmentAddLessonBinding
import com.dk.organizeu.student_activity.adapter.TimetableAdapter
import com.dk.organizeu.student_activity.data_class.TimetableItem
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class AddLessonFragment : Fragment(),AddLessonDialog.LessonListener {

    companion object {
        lateinit var academicYear:String
        lateinit var semesterNumber:String
        lateinit var academicType:String
        lateinit var className:String
        var selectedTab:Int=0
        fun newInstance() = AddLessonFragment()
    }

    private lateinit var viewModel: AddLessonViewModel
    private lateinit var binding: FragmentAddLessonBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore


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
                requireArguments().apply {
                    AddLessonFragment.apply {
                        academicYear = getString("academic_year",null)
                        academicType = getString("academic_type",null)
                        semesterNumber = getString("academic_semester",null)
                        className = getString("academic_class",null)
                    }
                }
                initRecyclerView()
                initLesson(1)
                btnAddLesson.setOnClickListener {
                    val dialogFragment = AddLessonDialog(this@AddLessonFragment)
                    dialogFragment.show(childFragmentManager, "customDialog")
                }

                tbLayoutWeekDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
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
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                })
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                timetableData.clear()
                lessonRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                timetableAdapter = TimetableAdapter(timetableData)
                lessonRecyclerView.adapter = timetableAdapter
            }
        }
    }
    private fun initLesson(weekDay:Int)
    {
        binding.apply {
            viewModel.apply {
                timetableData.clear()
                db.collection("academic")
                    .document("${academicYear}_${academicType}")
                    .collection("semester")
                    .document(semesterNumber)
                    .collection("class")
                    .document(className)
                    .collection("timetable")
                    .document(Weekday.getWeekdayNameByNumber(weekDay))
                    .collection("weekday")
                    .orderBy("start_time")
                    .get()
                    .addOnSuccessListener {documents->
                        var counter:Int = 1
                        for(document in documents)
                        {
                            val lessonItem = TimetableItem(
                                document.get("class_name").toString(),
                                document.get("subject_name").toString(),
                                document.get("subject_code").toString(),
                                document.get("location").toString(),
                                document.get("start_time").toString(),
                                document.get("end_time").toString(),
                                document.get("duration").toString(),
                                document.get("type").toString(),
                                document.get("faculty").toString(),
                                counter
                            )
                            counter++
                            timetableData.add(lessonItem)

                        }
                        timetableAdapter = TimetableAdapter(timetableData)
                        lessonRecyclerView.adapter = timetableAdapter
                    }
            }
        }
    }

    override fun onAddLesson() {
        initLesson(selectedTab+1)
    }
}