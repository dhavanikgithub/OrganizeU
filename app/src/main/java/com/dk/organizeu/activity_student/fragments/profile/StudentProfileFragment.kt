package com.dk.organizeu.activity_student.fragments.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentStudentProfileBinding
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.pojo.StudentPojo
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction

class StudentProfileFragment : Fragment() {

    private lateinit var viewModel: StudentProfileViewModel
    private lateinit var binding: FragmentStudentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_student_profile, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[StudentProfileViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                studentPojo = getStudentLocal(requireContext())

                txtEditAcademic.setOnClickListener {
                    UtilFunction.underConstructionDialog(requireContext())
                }
                txtEditSemester.setOnClickListener {
                    UtilFunction.underConstructionDialog(requireContext())
                }
                txtEditClass.setOnClickListener {
                    UtilFunction.underConstructionDialog(requireContext())
                }
                txtEditBatch.setOnClickListener {
                    UtilFunction.underConstructionDialog(requireContext())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SharedPreferencesManager.addValue(requireContext(),"activeFragment",R.id.studentProfileFragment)
    }


    fun getStudentLocal(context: Context): StudentPojo {
        val id = SharedPreferencesManager.getString(context, StudentLocalDBKey.ID.displayName, "")
        val name = SharedPreferencesManager.getString(context, StudentLocalDBKey.NAME.displayName, "")
        val academicYear = SharedPreferencesManager.getString(context, StudentLocalDBKey.ACADEMIC_YEAR.displayName, "")
        val academicType = SharedPreferencesManager.getString(context, StudentLocalDBKey.ACADEMIC_TYPE.displayName, "")
        val semester = SharedPreferencesManager.getString(context, StudentLocalDBKey.SEMESTER.displayName, "")
        val className = SharedPreferencesManager.getString(context, StudentLocalDBKey.CLASS.displayName, "")
        val batchName = SharedPreferencesManager.getString(context, StudentLocalDBKey.BATCH.displayName, "")

        return StudentPojo(id, name, academicYear, academicType, semester, className, batchName,"")
    }

}