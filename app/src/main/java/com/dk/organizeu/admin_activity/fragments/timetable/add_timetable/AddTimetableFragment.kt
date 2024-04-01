package com.dk.organizeu.admin_activity.fragments.timetable.add_timetable

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.dialog_box.AddLessonDialog
import com.dk.organizeu.admin_activity.dialog_box.AddSubjectDialog
import com.dk.organizeu.databinding.FragmentAddTimetableBinding
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.android.material.tabs.TabLayout

class AddTimetableFragment : Fragment() {

    companion object {
        lateinit var academicYear:String
        lateinit var semesterNumber:String
        lateinit var academicType:String
        lateinit var className:String
        var selectedTab:Int=0
        fun newInstance() = AddTimetableFragment()
    }

    private lateinit var viewModel: AddTimetableViewModel
    private lateinit var binding: FragmentAddTimetableBinding
    private lateinit var progressDialog: CustomProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_timetable, container, false)
        binding = FragmentAddTimetableBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddTimetableViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                requireArguments().apply {
                    AddTimetableFragment.apply {
                        academicYear = getString("academic_year",null)
                        academicType = getString("academic_type",null)
                        semesterNumber = getString("academic_semester",null)
                        className = getString("academic_class",null)
                    }
                }
                btnAddLesson.setOnClickListener {
                    val dialogFragment = AddLessonDialog()
                    dialogFragment.show(childFragmentManager, "customDialog")
                }

                weekDayTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        selectedTab = tab.position
                        when (tab.position) {
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
}