package com.dk.organizeu.activity_admin.fragments.timetable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.databinding.FragmentTimetableBinding
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimetableFragment : Fragment() {

    companion object {
        fun newInstance() = TimetableFragment()
        const val TAG = "OrganizeU-TimetableFragment"
    }

    private lateinit var viewModel: TimetableViewModel
    private lateinit var binding: FragmentTimetableBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        binding = FragmentTimetableBinding.bind(view)
        viewModel = ViewModelProvider(this)[TimetableViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                try {
                    loadAcademicYearDropDown()

                    btnGoToTimetable.isEnabled=false

                    if (selectedAcademicTypeItem==null)
                    {
                        tlAcademicType.isEnabled=false
                    }
                    else{
                        if(selectedAcademicYearItem!=null)
                        {
                            loadAcademicTypeDropDown()
                        }
                    }

                    if(selectedSemesterItem==null)
                    {
                        semTIL.isEnabled=false
                    }
                    else{
                        if(selectedAcademicYearItem!=null && selectedAcademicTypeItem!=null)
                        {
                            loadSemesterDropDown()
                        }
                    }

                    if(selectedClassItem==null)
                    {

                        classTIL.isEnabled=false
                    }
                    else{
                        if(selectedAcademicYearItem!=null && selectedAcademicTypeItem!=null && selectedSemesterItem!=null)
                        {
                            loadClassDropDown()
                        }
                    }

                    if(selectedAcademicYearItem!=null && selectedAcademicTypeItem!=null && selectedSemesterItem!=null && selectedClassItem!=null)
                    {
                        btnGoToTimetable.isEnabled=true
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    try {
                        selectedAcademicYearItem = parent.getItemAtPosition(position).toString()
                        clearAcademicType()
                        clearSemester()
                        clearClass()
                        loadAcademicTypeDropDown()

                        semTIL.isEnabled=false
                        classTIL.isEnabled=false
                        btnGoToTimetable.isEnabled=false
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        selectedAcademicTypeItem = parent.getItemAtPosition(position).toString()
                        clearSemester()
                        clearClass()
                        loadSemesterDropDown()

                        classTIL.isEnabled=false
                        btnGoToTimetable.isEnabled=false
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }

                }

                semACTV.setOnItemClickListener { parent, view, position, id ->
                    try {
                        selectedSemesterItem = parent.getItemAtPosition(position).toString()
                        clearClass()
                        loadClassDropDown()

                        btnGoToTimetable.isEnabled=false
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                classACTV.setOnItemClickListener { parent, view, position, id ->
                    try {
                        selectedClassItem = parent.getItemAtPosition(position).toString()
                        btnGoToTimetable.isEnabled=true
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                btnGoToTimetable.setOnClickListener {
                    try {
                        if(selectedAcademicYearItem!=null && selectedAcademicTypeItem!=null && selectedSemesterItem!=null && selectedClassItem!=null)
                        {
                            val bundle = Bundle().apply {
                                putString("academic_year", "${selectedAcademicYearItem}")
                                putString("academic_type", "${selectedAcademicTypeItem}")
                                putString("academic_semester", "${selectedSemesterItem}")
                                putString("academic_class", "${selectedClassItem}")
                            }
                            findNavController().navigate(R.id.addTimetableFragment,bundle)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }
            }
        }
    }


    fun clearSemester()
    {
        binding.apply {
            viewModel.apply {
                try {
                    semTIL.isEnabled=false
                    semACTV.setText("")
                    semesterList.clear()
                    if(semesterAdapter!=null)
                    {
                        semesterAdapter!!.notifyDataSetChanged()
                    }
                    selectedSemesterItem=null
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    fun clearClass()
    {
        binding.apply {
            viewModel.apply {
                try {
                    classTIL.isEnabled=false
                    classACTV.setText("")
                    classList.clear()
                    if(classAdapter!=null)
                    {
                        classAdapter!!.notifyDataSetChanged()
                    }
                    selectedClassItem=null
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    fun clearAcademicType()
    {
        binding.apply {
            viewModel.apply {
                try {
                    tlAcademicType.isEnabled=false
                    actAcademicType.setText("")
                    academicTypeList.clear()
                    if(academicTypeAdapter!=null)
                    {
                        academicTypeAdapter!!.notifyDataSetChanged()
                    }
                    selectedAcademicTypeItem=null
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    fun loadAcademicYearDropDown()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO)
                {
                    try {
                        academicYearList.clear()
                        val documents = AcademicRepository.getAllAcademicDocuments()
                        for(document in documents)
                        {
                            val academicItem = document.id.split('_')
                            if(!academicYearList.contains(academicItem[0]))
                            {
                                academicYearList.add(academicItem[0])
                            }
                        }

                        withContext(Dispatchers.Main)
                        {
                            try {
                                academicYearAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,academicYearList)
                                actAcademicYear.setAdapter(academicYearAdapter)
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
    }


    fun loadAcademicTypeDropDown()
    {
        binding.apply {
            viewModel.apply {
                try {
                    tlAcademicType.isEnabled=false
                    academicTypeList.clear()
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val evenExists =
                                isAcademicDocumentExists("${selectedAcademicYearItem!!}_${AcademicType.EVEN.name}")
                            if (evenExists) {
                                academicTypeList.add(AcademicType.EVEN.name)
                            }

                            val oddExists =
                                isAcademicDocumentExists("${selectedAcademicYearItem!!}_${AcademicType.ODD.name}")
                            if (oddExists) {
                                academicTypeList.add(AcademicType.ODD.name)
                            }

                            academicTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeList)
                            actAcademicType.setAdapter(academicTypeAdapter)
                            tlAcademicType.isEnabled=true
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }

                    }

                    MainScope().launch{
                        job.join()
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    fun loadSemesterDropDown()
    {
        binding.apply {
            viewModel.apply {
                try {
                    semTIL.isEnabled=false
                    semesterList.clear()
                    val academicDocumentId = "${selectedAcademicYearItem}_${selectedAcademicTypeItem}"
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
                            for(document in documents)
                            {
                                semesterList.add(document.id)
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    semesterAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,semesterList)
                                    semACTV.setAdapter(semesterAdapter)
                                    semTIL.isEnabled=true
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

    fun loadClassDropDown()
    {
        binding.apply {
            viewModel.apply {
                try {
                    classTIL.isEnabled=false
                    classList.clear()
                    val academicDocumentId = "${selectedAcademicYearItem}_${selectedAcademicTypeItem}"
                    val semesterDocumentId = selectedSemesterItem
                    if(semesterDocumentId!=null)
                    {
                        MainScope().launch(Dispatchers.IO)
                        {
                            val documents = ClassRepository.getAllClassDocuments(academicDocumentId,semesterDocumentId)
                            for(document in documents)
                            {
                                classList.add(document.id)
                            }
                            withContext(Dispatchers.Main){
                                try {
                                    classAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,classList)
                                    classACTV.setAdapter(classAdapter)
                                    classTIL.isEnabled=true
                                } catch (e: Exception) {
                                    Log.e(TAG,e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

            }
        }
    }
}