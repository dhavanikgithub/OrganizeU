package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_sem

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.AdminActivity
import com.dk.organizeu.admin_activity.adapter.AddSemAdapter
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.databinding.FragmentAddSemBinding
import com.dk.organizeu.model.AcademicPojo
import com.dk.organizeu.model.AcademicPojo.Companion.isAcademicDocumentExists
import com.dk.organizeu.model.SemesterPojo
import com.dk.organizeu.utils.CustomProgressDialog
import kotlinx.coroutines.*

class AddSemFragment : Fragment() {

    companion object {
        var viewModel2:AddAcademicViewModel?=null
        fun newInstance(viewModel2:AddAcademicViewModel):AddSemFragment{
            AddSemFragment.viewModel2=viewModel2
            return AddSemFragment()
        }
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
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {

                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                    academicSemTIL.isEnabled=false
                    tlAcademicType.isEnabled=false
                    btnAddSem.isEnabled=false
                    clearactAcademicType()
                    clearAcademicSemACTV()
                    academicSemList.clear()
                    academicSemAdapter.notifyDataSetChanged()

                    val job = lifecycleScope.launch(Dispatchers.Main) {
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
                }
                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                    academicSemTIL.isEnabled=false
                    btnAddSem.isEnabled=false
                    clearAcademicSemACTV()
                    academicSemList.clear()
                    academicSemAdapter.notifyDataSetChanged()
                    progressDialog.start("Loading Semester...")
                    MainScope().launch(Dispatchers.IO)
                    {

                        academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                        if(academicDocumentId!=null)
                        {
                            val documents = SemesterPojo.getAllSemesterDocuments(academicDocumentId!!)
                            for (document in documents) {
                                academicSemList.add(document.id)
                            }
                        }
                        withContext(Dispatchers.Main){
                            academicSemAdapter.notifyDataSetChanged()
                            loadAcademicSemACTV()
                            academicSemTIL.isEnabled=true
                            progressDialog.stop()
                        }
                    }


                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                    btnAddSem.isEnabled=true
                }
                btnAddSem.setOnClickListener {
                    if(academicTypeSelectedItem!=null && academicYearSelectedItem!=null && academicSemSelectedItem!=null)
                    {
                        MainScope().launch(Dispatchers.IO){
                            academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                            semesterDocumentId = academicSemSelectedItem
                            if(academicDocumentId!=null && semesterDocumentId!=null)
                            {
                                val inputHashMap = hashMapOf(
                                    "sem" to academicSemSelectedItem!!
                                )
                                SemesterPojo.insertSemesterDocuments(academicDocumentId!!,semesterDocumentId!!, inputHashMap,{
                                    academicSemList.add(academicSemSelectedItem!!)
                                    academicSemAdapter.notifyItemInserted(academicSemAdapter.itemCount)
                                    clearAcademicSemACTV()
                                    loadAcademicSemACTV()
                                    Toast.makeText(requireContext(),"Sem Added",Toast.LENGTH_SHORT).show()
                                },{

                                })
                            }
                        }

                    }
                }

            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                academicSemLayoutManager = LinearLayoutManager(requireContext())
                recyclerView.layoutManager = academicSemLayoutManager
                progressDialog.start("Loading Semester...")
                MainScope().launch(Dispatchers.IO)
                {

                    academicSemList.clear()
                    academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                    if(academicDocumentId!=null)
                    {
                        val documents = SemesterPojo.getAllSemesterDocuments(academicDocumentId!!)
                        for (document in documents) {
                            academicSemList.add(document.id)
                        }
                    }
                    withContext(Dispatchers.Main){
                        academicSemAdapter = AddSemAdapter(academicSemList)
                        recyclerView.adapter = academicSemAdapter
                        loadAcademicSemACTV()
                        progressDialog.stop()
                    }
                }

            }
        }
    }

    private fun clearactAcademicType()
    {
        binding.apply {
            viewModel.apply {
                academicTypeSelectedItem=null
                academicTypeItemList.clear()
                academicTypeItemAdapter.notifyDataSetChanged()
                actAcademicType.setText("")
            }
        }
    }

    private fun clearAcademicSemACTV()
    {
        binding.apply {
            viewModel.apply {
                academicSemSelectedItem=null
                academicSemItemList.clear()
                academicSemItemAdapter.notifyDataSetChanged()
                academicSemACTV.setText("")
            }
        }
    }

    private fun loadActAcademicYear()
    {
        binding.apply {
            viewModel.apply {
                academicYearItemList.clear()
                MainScope().launch(Dispatchers.IO)
                {
                    val documents = AcademicPojo.getAllAcademicDocuments()
                    for (document in documents) {
                        val academicItem = document.id.split('_')
                        if(!academicYearItemList.contains(academicItem[0]))
                        {
                            academicYearItemList.add(academicItem[0])
                        }
                    }
                    withContext(Dispatchers.Main)
                    {
                        academicYearItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearItemList)
                        actAcademicYear.setAdapter(academicYearItemAdapter)
                    }
                }

            }
        }
    }

    private fun loadActAcademicType()
    {
        binding.apply {
            viewModel.apply {
                tlAcademicType.isEnabled=false
                academicTypeItemList.clear()
                val job = lifecycleScope.launch(Dispatchers.Main) {
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
                }

                MainScope().launch{
                    job.join()
                }

            }
        }
    }

    private fun loadAcademicSemACTV()
    {
        binding.apply {
            viewModel.apply {
                academicSemItemList.clear()
                if(academicTypeSelectedItem==AcademicType.EVEN.name)
                {
                    academicSemItemList.addAll(UtilFunction.evenSemList)
                }
                else if(academicTypeSelectedItem==AcademicType.ODD.name){
                    academicSemItemList.addAll(UtilFunction.oddSemList)
                }
                for(item in academicSemList)
                {
                    academicSemItemList.remove(item.toInt())
                }
                academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                academicSemACTV.setAdapter(academicSemItemAdapter)
            }
        }
    }


}