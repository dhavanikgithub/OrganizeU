package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.adapter.SemAdapter
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.databinding.FragmentAddSemBinding
import com.dk.organizeu.firebase.key_mapping.SemesterCollection
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.repository.SemesterRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.*

class AddSemFragment : Fragment() {

    companion object {
        var viewModel2:AddAcademicViewModel?=null
        fun newInstance(viewModel2:AddAcademicViewModel):AddSemFragment{
            AddSemFragment.viewModel2=viewModel2
            return AddSemFragment()
        }

        const val TAG = "OrganizeU-AddSemFragment"
    }

    private lateinit var viewModel: AddSemViewModel
    private lateinit var binding: FragmentAddSemBinding
    private lateinit var academicSemLayoutManager: LinearLayoutManager
    private lateinit var progressDialog: CustomProgressDialog
    var academicDocumentId:String? = null
    var semesterDocumentId:String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_sem, container, false)
        binding = FragmentAddSemBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddSemViewModel::class.java]
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
                    btnAddSem.isEnabled=false
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
                        loadActAcademicYear()
                        loadActAcademicType()
                        initRecyclerView()
                        if(academicTypeSelectedItem!=null)
                        {

                            academicSemTIL.isEnabled=true
                        }
                        if(academicSemSelectedItem!=null)
                        {
                            btnAddSem.isEnabled=true
                        }
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
                    swipeRefresh.isRefreshing = false
                }

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    try {
                        academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                        academicSemTIL.isEnabled=false
                        tlAcademicType.isEnabled=false
                        btnAddSem.isEnabled=false
                        clearactAcademicType()
                        clearAcademicSemACTV()
                        academicSemList.clear()
                        academicSemAdapter.notifyDataSetChanged()

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

                        /*isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}"){
                                if(it)
                                {
                                    academicTypeItemList.add(AcademicType.EVEN.name)
                                }
                            }
                            isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}"){
                                if(it)
                                {
                                    academicTypeItemList.add(AcademicType.ODD.name)
                                }
                            }

                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            actAcademicType.setAdapter(academicTypeItemAdapter)*/
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)

                    }
                }
                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    try {
                        academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                        academicSemTIL.isEnabled=false
                        btnAddSem.isEnabled=false
                        clearAcademicSemACTV()
                        academicSemList.clear()
                        academicSemAdapter.notifyDataSetChanged()
                        progressDialog.start("Loading Semester...")
                        MainScope().launch(Dispatchers.IO)
                        {
                            try {
                                academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                                if(academicDocumentId!=null)
                                {
                                    val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId!!)
                                    for (document in documents) {
                                        academicSemList.add(document.id)
                                    }
                                }
                                withContext(Dispatchers.Main){
                                    try {
                                        academicSemAdapter.notifyDataSetChanged()
                                        loadAcademicSemACTV()
                                        academicSemTIL.isEnabled=true
                                        progressDialog.stop()
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

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    try {
                        academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                        btnAddSem.isEnabled=true
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }
                btnAddSem.setOnClickListener {
                    try {
                        if(academicTypeSelectedItem!=null && academicYearSelectedItem!=null && academicSemSelectedItem!=null)
                        {
                            MainScope().launch(Dispatchers.IO){
                                try {
                                    academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                                    semesterDocumentId = academicSemSelectedItem
                                    if(academicDocumentId!=null && semesterDocumentId!=null)
                                    {
                                        val inputHashMap = hashMapOf(
                                            SemesterCollection.SEMESTER.displayName to academicSemSelectedItem!!
                                        )
                                        SemesterRepository.insertSemesterDocuments(academicDocumentId!!,semesterDocumentId!!, inputHashMap,{
                                            try {
                                                academicSemList.add(academicSemSelectedItem!!)
                                                academicSemAdapter.notifyItemInserted(academicSemAdapter.itemCount)
                                                clearAcademicSemACTV()
                                                loadAcademicSemACTV()
                                                Toast.makeText(requireContext(),"Sem Added",Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Log.e(TAG,e.message.toString())
                                                requireContext().unexpectedErrorMessagePrint(e)
                                            }
                                        },{
                                            Log.e(TAG,it.message.toString())
                                            requireContext().unexpectedErrorMessagePrint(it)
                                        })
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
                try {
                    showProgressBar(rvSemester,progressBar)
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            academicSemList.clear()
                            academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                            if(academicDocumentId!=null)
                            {
                                val documents = SemesterRepository.getAllSemesterDocuments(academicDocumentId!!)
                                for (document in documents) {
                                    academicSemList.add(document.id)
                                }
                            }
                            withContext(Dispatchers.Main){
                                try {
                                    academicSemAdapter = SemAdapter(academicSemList)
                                    academicSemLayoutManager = LinearLayoutManager(requireContext())
                                    rvSemester.layoutManager = academicSemLayoutManager
                                    rvSemester.adapter = academicSemAdapter
                                    loadAcademicSemACTV()
                                    delay(500)
                                    hideProgressBar(rvSemester,progressBar)
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

    private fun loadActAcademicYear()
    {
        binding.apply {
            viewModel.apply {
                try {
                    academicYearItemList.clear()
                    MainScope().launch(Dispatchers.IO)
                    {
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
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

            }
        }
    }

    private fun loadActAcademicType()
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
                try {
                    academicSemItemList.clear()
                    if(academicTypeSelectedItem== AcademicType.EVEN.name)
                    {
                        academicSemItemList.addAll(UtilFunction.evenSemList)
                    }
                    else if(academicTypeSelectedItem== AcademicType.ODD.name){
                        academicSemItemList.addAll(UtilFunction.oddSemList)
                    }
                    for(item in academicSemList)
                    {
                        academicSemItemList.remove(item.toInt())
                    }
                    academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                    academicSemACTV.setAdapter(academicSemItemAdapter)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


}