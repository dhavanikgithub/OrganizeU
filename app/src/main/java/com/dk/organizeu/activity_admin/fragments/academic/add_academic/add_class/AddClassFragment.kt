package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_class

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.adapter.ClassAdapter
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.databinding.FragmentAddClassBinding
import com.dk.organizeu.firebase.key_mapping.ClassCollection
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.repository.ClassRepository
import com.dk.organizeu.repository.ClassRepository.Companion.isClassDocumentExists
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.*

class AddClassFragment : Fragment() {

    companion object {
        var viewModel2: AddAcademicViewModel?=null
        fun newInstance(viewModel2: AddAcademicViewModel): AddClassFragment {
            AddClassFragment.viewModel2=viewModel2
            return AddClassFragment()
        }

        const val TAG = "OrganizeU-AddClassFragment"
    }

    private lateinit var viewModel: AddClassViewModel
    private lateinit var binding: FragmentAddClassBinding
    private lateinit var progressDialog: CustomProgressDialog
    var academicDocumentId:String? = null
    var semesterDocumentId:String? = null
    var classDocumentId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_class, container, false)
        binding = FragmentAddClassBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddClassViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                try {
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                    academicSemTIL.isEnabled=false
                    academicClassTIL.isEnabled=false
                    btnAddClass.isEnabled=false
                    if (AddAcademicFragment.academicType!=null && AddAcademicFragment.academicYear!=null)
                    {
                        if(academicYearSelectedItem==null)
                        {
                            academicYearSelectedItem = AddAcademicFragment.academicYear
                        }
                        if(academicTypeSelectedItem==null)
                        {
                            academicTypeSelectedItem = AddAcademicFragment.academicType
                        }

                        actAcademicYear.setText(academicYearSelectedItem)
                        actAcademicType.setText(academicTypeSelectedItem)
                        if(academicSemSelectedItem!=null)
                        {
                            academicSemACTV.setText(academicSemSelectedItem)
                        }
                        loadactAcademicYear()
                        loadactAcademicType()
                        initRecyclerView()
                        if(academicTypeSelectedItem!=null)
                        {
                            academicSemTIL.isEnabled=true
                        }
                        if(academicSemSelectedItem!=null)
                        {
                            academicClassTIL.isEnabled=true
                        }
                        if(classET.text.toString()!="")
                        {
                            btnAddClass.isEnabled=true
                        }
                    }
                    if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null)
                    {
                        loadAcademicSemACTV()
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
                swipeRefresh.setOnRefreshListener {
                    initRecyclerView()
                    swipeRefresh.isRefreshing=false
                }
                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    try {
                        academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                        tlAcademicType.isEnabled=false
                        academicSemTIL.isEnabled=false
                        academicClassTIL.isEnabled=false
                        btnAddClass.isEnabled=false
                        clearactAcademicType()
                        clearAcademicSemACTV()
                        academicClassList.clear()
                        academicClassAdapter.notifyDataSetChanged()

                        val job = lifecycleScope.launch(Dispatchers.Main) {
                            try {
                                val evenExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                                if (evenExists) {
                                    academicTypeItemList.add(AcademicType.EVEN.name)
                                }

                                val oddExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
                                if (oddExists) {
                                    academicTypeItemList.add(AcademicType.ODD.name)
                                }

                                academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                                actAcademicType.setAdapter(academicTypeItemAdapter)
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
                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                        academicSemTIL.isEnabled=false
                        academicClassTIL.isEnabled=false
                        btnAddClass.isEnabled=false
                        clearAcademicSemACTV()
                        loadAcademicSemACTV()
                        academicClassList.clear()
                        academicClassAdapter.notifyDataSetChanged()
                        academicSemTIL.isEnabled=true
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    try {
                        academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                        academicClassTIL.isEnabled=false
                        btnAddClass.isEnabled=false
                        academicClassList.clear()
                        academicClassAdapter.notifyDataSetChanged()
                        initRecyclerView()
                        academicClassTIL.isEnabled=true
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                classET.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        try {
                            btnAddClass.isEnabled = s.toString() != ""
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                btnAddClass.setOnClickListener {
                    try {
                        if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null && academicSemSelectedItem!=null && classET.text!!.toString().isNotBlank() && classET.text!!.toString().isNotEmpty())
                        {

                            academicDocumentId = "${academicYearSelectedItem}_${academicTypeSelectedItem}"
                            semesterDocumentId = academicSemSelectedItem
                            classDocumentId = classET.text.toString()
                            isClassDocumentExists(academicDocumentId!!,semesterDocumentId!!,classDocumentId!!){
                                try {
                                    if(!it)
                                    {
                                        MainScope().launch(Dispatchers.IO)
                                        {
                                            val inputHashMap = hashMapOf(
                                                ClassCollection.CLASS.displayName to classET.text.toString()
                                            )
                                            ClassRepository.insertClassDocument(academicDocumentId!!,semesterDocumentId!!,classDocumentId!!,inputHashMap,{
                                                try {
                                                    academicClassList.add(classET.text.toString())
                                                    academicClassAdapter.notifyItemInserted(academicClassAdapter.itemCount)
                                                    classET.setText("")
                                                    Toast.makeText(requireContext(),"Class Added", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Log.e(TAG,e.message.toString())
                                                    requireContext().unexpectedErrorMessagePrint(e)
                                                }
                                            },{
                                                Log.e(TAG,it.message.toString())
                                                requireContext().unexpectedErrorMessagePrint(it)
                                            })
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG,e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
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

    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                showProgressBar(rvClass,progressBar)
                MainScope().launch(Dispatchers.IO)
                {
                    try {
                        academicClassList.clear()
                        academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                        semesterDocumentId = academicSemSelectedItem
                        if(academicDocumentId!="null" && semesterDocumentId!=null)
                        {
                            val documents = ClassRepository.getAllClassDocuments(academicDocumentId!!,semesterDocumentId!!)
                            for (document in documents) {
                                academicClassList.add(document.id)
                            }
                        }
                        withContext(Dispatchers.Main)
                        {
                            try {
                                academicClassAdapter = ClassAdapter(academicClassList)
                                rvClass.layoutManager = LinearLayoutManager(requireContext())
                                rvClass.adapter = academicClassAdapter
                                delay(500)
                                hideProgressBar(rvClass,progressBar)
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


    private fun clearactAcademicType()
    {
        binding.apply {
            viewModel.apply {
                try {
                    academicTypeSelectedItem=null
                    academicTypeItemList.clear()
                    academicTypeItemAdapter.notifyDataSetChanged()
                    actAcademicType.setText("")
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun clearAcademicSemACTV()
    {
        binding.apply {
            viewModel.apply {
                try {
                    academicSemSelectedItem=null
                    academicSemItemList.clear()
                    academicSemItemAdapter.notifyDataSetChanged()
                    academicSemACTV.setText("")
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun loadactAcademicYear()
    {
        binding.apply {
            viewModel.apply {

                academicYearItemList.clear()
                MainScope().launch(Dispatchers.IO){
                    try {
                        val documents = AcademicRepository.getAllAcademicDocuments()
                        for (document in documents) {
                            val academicItem = document.id.split('_')
                            if(!academicYearItemList.contains(academicItem[0]))
                            {
                                academicYearItemList.add(academicItem[0])
                            }
                        }
                        withContext(Dispatchers.Main)
                        {
                            try {
                                academicYearItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearItemList)
                                actAcademicYear.setAdapter(academicYearItemAdapter)
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

    private fun loadactAcademicType()
    {
        binding.apply {
            viewModel.apply {
                try {
                    tlAcademicType.isEnabled=false
                    academicTypeItemList.clear()
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val evenExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                            if (evenExists) {
                                academicTypeItemList.add(AcademicType.EVEN.name)
                            }

                            val oddExists = isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
                            if (oddExists) {
                                academicTypeItemList.add(AcademicType.ODD.name)
                            }

                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            actAcademicType.setAdapter(academicTypeItemAdapter)
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

    private fun loadAcademicSemACTV()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO)
                {
                    try {
                        academicSemItemList.clear()
                        val academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                        val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId)
                        for (document in documents) {
                            academicSemItemList.add(document.id.toInt())
                        }
                        withContext(Dispatchers.Main)
                        {
                            try {
                                academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                                academicSemACTV.setAdapter(academicSemItemAdapter)
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

}