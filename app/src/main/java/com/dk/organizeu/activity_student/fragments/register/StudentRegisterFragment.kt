package com.dk.organizeu.activity_student.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentStudentRegisterBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.pojo.AcademicPojo.Companion.toAcademicPojo
import com.dk.organizeu.repository.AcademicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentRegisterFragment : Fragment() {

    private lateinit var viewModel: StudentRegisterViewModel
    private lateinit var binding: FragmentStudentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_student_register, container, false)
        viewModel = ViewModelProvider(this)[StudentRegisterViewModel::class.java]
        binding = DataBindingUtil.bind(view)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {
            binding.apply {
                tlSemester.isEnabled = false
                tlClass.isEnabled = false
                tlBatch.isEnabled = false
                chipGroupAcademicType.isEnabled = false

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    selectedAcademicYear = parent.getItemAtPosition(position).toString()
                    chipGroupAcademicType.isEnabled = false
                    tlSemester.isEnabled = false
                    tlClass.isEnabled = false
                    tlBatch.isEnabled = false
                    initChipGroup()
                    chipGroupAcademicType.isEnabled = true
                }

                txtLogin.setOnClickListener {
                    gotoLogin()

                }
            }
        }



    }

    fun clearSemester() {
        binding.apply {
            viewModel.apply {

            }
        }
    }

    fun clearClass() {
        binding.apply {
            viewModel.apply {

            }
        }
    }

    fun clearBatch() {
        binding.apply {
            viewModel.apply {

            }
        }
    }

    private fun gotoLogin()
    {
        findNavController().popBackStack()
        findNavController().navigate(R.id.studentLoginFragment)
    }

    private fun initChipGroup()
    {
        MainScope().launch(Dispatchers.IO)
        {
            binding.apply {
                viewModel.apply {
                    val evenAcademicPojo = AcademicRepository.getAcademicPojoByYearAndType(selectedAcademicYear!!,AcademicType.EVEN.name)
                    val oddAcademicPojo = AcademicRepository.getAcademicPojoByYearAndType(selectedAcademicYear!!,AcademicType.ODD.name)

                   withContext(Dispatchers.Main)
                   {
                       if(evenAcademicPojo==null)
                       {
                           chipEven.visibility = View.GONE
                       }
                       else{
                           chipEven.visibility = View.VISIBLE
                       }

                       if(oddAcademicPojo==null)
                       {
                           chipOdd.visibility = View.GONE
                       }
                       else{
                           chipOdd.visibility = View.VISIBLE
                       }
                   }
                }
            }

        }
    }

    private fun initAcademicYearDropdown(){
        MainScope().launch(Dispatchers.IO){
            binding.apply {
                viewModel.apply {
                    academicYearList.clear()
                    val academicDocuments = AcademicRepository.getAllAcademicDocuments()
                    academicDocuments.map {
                        val temp = it.toAcademicPojo()
                        if(!academicYearList.contains(temp.year))
                        {
                            academicYearList.add(temp.year)
                        }
                    }
                   withContext(Dispatchers.Main)
                   {
                       academicYearAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,academicYearList)
                       actAcademicYear.setAdapter(academicYearAdapter)
                   }
                }
            }
        }
    }
}