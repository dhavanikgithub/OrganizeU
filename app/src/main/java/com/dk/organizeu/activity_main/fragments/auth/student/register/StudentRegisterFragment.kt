package com.dk.organizeu.activity_main.fragments.auth.student.register

import android.os.Bundle
import android.util.Log
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
import com.dk.organizeu.pojo.BatchPojo.Companion.toBatchPojo
import com.dk.organizeu.pojo.ClassPojo.Companion.toClassPojo
import com.dk.organizeu.pojo.SemesterPojo.Companion.toSemesterPojo
import com.dk.organizeu.pojo.StudentPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.BatchRepository
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.repository.StudentRepository
import com.dk.organizeu.repository.StudentRepository.Companion.toSHA256
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.trimExtraSpaces
import com.dk.organizeu.utils.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentRegisterFragment : Fragment() {

    private lateinit var viewModel: StudentRegisterViewModel
    private lateinit var binding: FragmentStudentRegisterBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_student_register, container, false)
        viewModel = ViewModelProvider(this)[StudentRegisterViewModel::class.java]
        binding = DataBindingUtil.bind(view)!!
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {
            binding.apply {
                tlSemester.isEnabled = false
                tlClass.isEnabled = false
                tlBatch.isEnabled = false
                chipEven.isEnabled = false
                chipOdd.isEnabled = false
                chipEven.isChecked = false
                chipOdd.isChecked = false
                initAcademicYearDropdown()

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    selectedAcademicYear = parent.getItemAtPosition(position).toString()
                    chipEven.isEnabled = false
                    chipOdd.isEnabled = false
                    chipEven.isChecked = false
                    chipOdd.isChecked = false
                    tlSemester.isEnabled = false
                    tlClass.isEnabled = false
                    tlBatch.isEnabled = false
                    clearSemester()
                    clearClass()
                    clearBatch()
                    initChipGroup()

                }



                chipEven.setOnClickListener {
                    if(chipEven.isChecked)
                    {
                        tlSemester.isEnabled = false
                        tlClass.isEnabled = false
                        tlBatch.isEnabled = false
                        clearSemester()
                        clearClass()
                        clearBatch()
                        selectedAcademicType = chipEven.text.toString()
                        initSemester()
                    }
                    else{
                        tlSemester.isEnabled = false
                        tlClass.isEnabled = false
                        tlBatch.isEnabled = false
                        clearSemester()
                        clearClass()
                        clearBatch()
                        selectedAcademicType = null
                    }
                }

                chipOdd.setOnClickListener {
                    if(chipOdd.isChecked)
                    {
                        tlSemester.isEnabled = false
                        tlClass.isEnabled = false
                        tlBatch.isEnabled = false
                        clearSemester()
                        clearClass()
                        clearBatch()
                        selectedAcademicType = chipOdd.text.toString()
                        initSemester()
                    }
                    else{
                        tlSemester.isEnabled = false
                        tlClass.isEnabled = false
                        tlBatch.isEnabled = false
                        clearSemester()
                        clearClass()
                        clearBatch()
                        selectedAcademicType = null
                    }
                }


                actSemester.setOnItemClickListener { parent, view, position, id ->
                    selectedSemester = parent.getItemAtPosition(position).toString()
                    tlClass.isEnabled = false
                    tlBatch.isEnabled = false
                    clearClass()
                    clearBatch()
                    initClass()
                }
                actBatch.setOnItemClickListener { parent, view, position, id ->
                    selectedBatch = parent.getItemAtPosition(position).toString()
                }

                actClass.setOnItemClickListener { parent, view, position, id ->
                    selectedClass = parent.getItemAtPosition(position).toString()
                    tlBatch.isEnabled = false
                    clearBatch()
                    initBatch()
                }
                btnRegister.setOnClickListener {

                    val studentId = etStudentId.text.toString()
                    val studentName = etStudentName.text.toString().trimExtraSpaces()
                    val plaintTextPassword = etPassword.text.toString()
                    if(selectedAcademicYear!=null && selectedAcademicType!=null && selectedClass !=null && selectedSemester!=null && selectedBatch != null)
                    {
                        if(Validation.validateEnrollment(studentId) && Validation.validateStudentName(studentName) && Validation.validatePassword(plaintTextPassword))
                        {
                            progressDialog.start("")
                            val cipherTextPassword = plaintTextPassword.toSHA256()
                            val studentPojo = StudentPojo(studentId,studentName,selectedAcademicYear!!,selectedAcademicType!!,selectedSemester!!,selectedClass!!,selectedBatch!!,cipherTextPassword)
                            StudentRepository.isStudentExistById(studentPojo.id){isExistById ->
                                if(isExistById)
                                {
                                    progressDialog.stop()
                                    requireContext().showToast("Student id already register")
                                    return@isStudentExistById
                                }
                                StudentRepository.isStudentExistByName(studentPojo.name){isExistByName ->
                                    if(isExistByName)
                                    {
                                        progressDialog.stop()
                                        requireContext().showToast("Student name already register")
                                        return@isStudentExistByName
                                    }
                                    StudentRepository.insertStudent(studentPojo){
                                        progressDialog.stop()
                                        if(it)
                                        {
                                            requireContext().showToast("Register Successfully")
                                            gotoLogin()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else{
                        Log.e("Register",selectedAcademicYear.toString())
                        Log.e("Register",selectedAcademicType.toString())
                        Log.e("Register",selectedSemester.toString())
                        Log.e("Register",selectedClass.toString())
                        Log.e("Register",selectedBatch.toString())
                    }
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
                selectedSemester=null
                semesterList.clear()
                actSemester.text.clear()
                semesterAdapter=null
            }
        }
    }

    fun clearClass() {
        binding.apply {
            viewModel.apply {
                selectedClass = null
                classList.clear()
                actClass.text.clear()
                classAdapter = null
            }
        }
    }

    fun clearBatch() {
        binding.apply {
            viewModel.apply {
                selectedBatch = null
                batchList.clear()
                actBatch.text.clear()
                batchAdapter = null
            }
        }
    }

    private fun gotoLogin()
    {
        findNavController().popBackStack()
        findNavController().navigate(R.id.studentLoginFragment)
    }

    private fun initSemester()
    {
        MainScope().launch(Dispatchers.IO)
        {
            viewModel.apply {
                binding.apply {
                    semesterList.clear()
                    val academicId = AcademicRepository.getAcademicIdByYearAndType(selectedAcademicYear!!,selectedAcademicType!!)
                    val allSemesterList = SemesterRepository.getAllSemesterDocuments(academicId!!)
                    allSemesterList.map {
                        semesterList.add(it.toSemesterPojo().name)
                    }
                    withContext(Dispatchers.Main)
                    {
                        semesterAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,semesterList)
                        actSemester.setAdapter(semesterAdapter)
                        tlSemester.isEnabled=true
                    }
                }
            }
        }
    }

    private fun initClass()
    {
        MainScope().launch(Dispatchers.IO)
        {
            viewModel.apply {
                binding.apply {
                    classList.clear()
                    val academicId = AcademicRepository.getAcademicIdByYearAndType(selectedAcademicYear!!,selectedAcademicType!!)
                    val semesterId = SemesterRepository.getSemesterIdByName(academicId!!,selectedSemester!!)
                    val allClassList = ClassRepository.getAllClassDocuments(academicId,semesterId!!)
                    allClassList.map {
                        classList.add(it.toClassPojo().name)
                    }

                    withContext(Dispatchers.Main)
                    {
                        classAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,classList)
                        actClass.setAdapter(classAdapter)
                        tlClass.isEnabled = true
                    }
                }
            }
        }
    }

    private fun initBatch()
    {
        MainScope().launch(Dispatchers.IO){
            viewModel.apply {
                binding.apply {
                    batchList.clear()
                    val academicId = AcademicRepository.getAcademicIdByYearAndType(selectedAcademicYear!!,selectedAcademicType!!)
                    val semesterId = SemesterRepository.getSemesterIdByName(academicId!!,selectedSemester!!)
                    val classId = ClassRepository.getClassIdByName(academicId,semesterId!!,selectedClass!!)
                    val allBatchList = BatchRepository.getAllBatchDocuments(academicId,semesterId,classId!!)
                    allBatchList.map {
                        batchList.add(it.toBatchPojo().name)
                    }
                    withContext(Dispatchers.Main)
                    {
                        batchAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,batchList)
                        actBatch.setAdapter(batchAdapter)
                        tlBatch.isEnabled = true
                    }
                }
            }
        }
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
                       chipEven.isEnabled = true
                       chipOdd.isEnabled = true
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