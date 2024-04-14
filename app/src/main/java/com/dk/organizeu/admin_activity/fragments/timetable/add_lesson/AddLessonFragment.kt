package com.dk.organizeu.admin_activity.fragments.timetable.add_lesson

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.dialog.AddLessonDialog
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.databinding.FragmentAddLessonBinding
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.LessonRepository.Companion.lessonDocumentToLessonObj
import com.dk.organizeu.adapter.LessonAdapter
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                showProgressBar()
                timetableData.clear()
                rvLesson.layoutManager = LinearLayoutManager(requireContext())
                lessonAdapter = LessonAdapter(timetableData)
                rvLesson.adapter = lessonAdapter
                hideProgressBar()
            }
        }
    }

    fun showProgressBar()
    {
        binding.rvLesson.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar()
    {
        binding.progressBar.visibility = View.GONE
        binding.rvLesson.visibility = View.VISIBLE
    }
    private fun initLesson(weekDay:Int)
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO){

                    timetableData.clear()
                    val academicDocumentId = "${academicYear}_${academicType}"
                    val semesterDocumentId = semesterNumber
                    val classDocumentId = className
                    val timetableDocumentId = Weekday.getWeekdayNameByNumber(weekDay)

                    val documents = LessonRepository.getAllLessonDocuments(academicDocumentId, semesterDocumentId, classDocumentId, timetableDocumentId,"start_time")
                    var counter = 1
                    for(document in documents)
                    {
                        val lessonItem = lessonDocumentToLessonObj(document,counter)
                        counter++
                        timetableData.add(lessonItem)
                    }

                    withContext(Dispatchers.Main)
                    {
                        lessonAdapter = LessonAdapter(timetableData)
                        rvLesson.adapter = lessonAdapter
                    }
                }
            }
        }
    }

    override fun onAddLesson() {
        initLesson(selectedTab+1)
    }

    override fun onConflict() {
        MainScope().launch(Dispatchers.Main)
        {
            Toast.makeText(requireContext(),"Lesson Already Exist", Toast.LENGTH_SHORT).show()
        }
    }
}