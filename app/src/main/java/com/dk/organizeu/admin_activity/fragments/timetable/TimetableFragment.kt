package com.dk.organizeu.admin_activity.fragments.timetable

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.util.UtilFunction
import com.dk.organizeu.databinding.FragmentTimetableBinding
import com.dk.organizeu.model.AcademicPojo.Companion.isAcademicDocumentExists
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TimetableFragment : Fragment() {

    companion object {
        fun newInstance() = TimetableFragment()
    }

    private lateinit var viewModel: TimetableViewModel
    private lateinit var binding: FragmentTimetableBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        binding = FragmentTimetableBinding.bind(view)
        viewModel = ViewModelProvider(this)[TimetableViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db = FirebaseFirestore.getInstance()
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
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
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {


                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    selectedAcademicYearItem = parent.getItemAtPosition(position).toString()
                    clearAcademicType()
                    clearSemester()
                    clearClass()
                    loadAcademicTypeDropDown()

                    semTIL.isEnabled=false
                    classTIL.isEnabled=false
                    btnGoToTimetable.isEnabled=false
                }

                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    selectedAcademicTypeItem = parent.getItemAtPosition(position).toString()
                    clearSemester()
                    clearClass()
                    loadSemesterDropDown()

                    classTIL.isEnabled=false
                    btnGoToTimetable.isEnabled=false

                }

                semACTV.setOnItemClickListener { parent, view, position, id ->
                    selectedSemesterItem = parent.getItemAtPosition(position).toString()
                    clearClass()
                    loadClassDropDown()

                    btnGoToTimetable.isEnabled=false
                }

                classACTV.setOnItemClickListener { parent, view, position, id ->
                    selectedClassItem = parent.getItemAtPosition(position).toString()
                    btnGoToTimetable.isEnabled=true
                }

                btnGoToTimetable.setOnClickListener {
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
                }
            }
        }
    }


    fun clearSemester()
    {
        binding.apply {
            viewModel.apply {
                semTIL.isEnabled=false
                semACTV.setText("")
                semesterList.clear()
                if(semesterAdapter!=null)
                {
                    semesterAdapter!!.notifyDataSetChanged()
                }
                selectedSemesterItem=null
            }
        }
    }

    fun clearClass()
    {
        binding.apply {
            viewModel.apply {
                classTIL.isEnabled=false
                classACTV.setText("")
                classList.clear()
                if(classAdapter!=null)
                {
                    classAdapter!!.notifyDataSetChanged()
                }
                selectedClassItem=null
            }
        }
    }

    fun clearAcademicType()
    {
        binding.apply {
            viewModel.apply {
                tlAcademicType.isEnabled=false
                actAcademicType.setText("")
                academicTypeList.clear()
                if(academicTypeAdapter!=null)
                {
                    academicTypeAdapter!!.notifyDataSetChanged()
                }
                selectedAcademicTypeItem=null
            }
        }
    }

    fun loadAcademicYearDropDown()
    {
        binding.apply {
            viewModel.apply {
                academicYearList.clear()
                db.collection("academic")
                    .get()
                    .addOnSuccessListener {  documents ->
                        for(document in documents)
                        {
                            val academicItem = document.id.split('_')
                            if(!academicYearList.contains(academicItem[0]))
                            {
                                academicYearList.add(academicItem[0])
                            }
                        }
                    }

                academicYearAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,academicYearList)
                actAcademicYear.setAdapter(academicYearAdapter)
            }
        }
    }


    fun loadAcademicTypeDropDown()
    {
        binding.apply {
            viewModel.apply {
                tlAcademicType.isEnabled=false
                academicTypeList.clear()
                val job = lifecycleScope.launch(Dispatchers.Main) {
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

                }

                MainScope().launch{
                    job.join()
                }
            }
        }
    }


    fun loadSemesterDropDown()
    {
        binding.apply {
            viewModel.apply {
                semTIL.isEnabled=false
                semesterList.clear()
                db.collection("academic")
                    .document("${selectedAcademicYearItem}_${selectedAcademicTypeItem}")
                    .collection("semester")
                    .get()
                    .addOnSuccessListener {documents->
                        for(document in documents)
                        {
                            semesterList.add(document.id)
                        }
                        semesterAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,semesterList)
                        semACTV.setAdapter(semesterAdapter)
                        semTIL.isEnabled=true
                    }
            }
        }
    }

    fun loadClassDropDown()
    {
        binding.apply {
            viewModel.apply {
                classTIL.isEnabled=false
                classList.clear()
                db.collection("academic")
                    .document("${selectedAcademicYearItem}_${selectedAcademicTypeItem}")
                    .collection("semester")
                    .document(selectedSemesterItem.toString())
                    .collection("class")
                    .get()
                    .addOnSuccessListener {documents->
                        for(document in documents)
                        {
                            classList.add(document.id)
                        }
                        classAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,classList)
                        classACTV.setAdapter(classAdapter)
                        classTIL.isEnabled=true
                    }
            }
        }
    }
}