package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_batch

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
import com.dk.organizeu.admin_activity.AdminActivity
import com.dk.organizeu.admin_activity.adapter.AddBatchAdapter
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.databinding.FragmentAddBatchBinding
import com.dk.organizeu.model.AcademicPojo
import com.dk.organizeu.model.AcademicPojo.Companion.isAcademicDocumentExists
import com.dk.organizeu.model.BatchPojo
import com.dk.organizeu.model.BatchPojo.Companion.insertBatchDocument
import com.dk.organizeu.model.BatchPojo.Companion.isBatchDocumentExists
import com.dk.organizeu.model.ClassPojo
import com.dk.organizeu.model.SemesterPojo
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class AddBatchFragment : Fragment() {

    companion object {
        fun newInstance() = AddBatchFragment()
    }

    private lateinit var viewModel: AddBatchViewModel
    private lateinit var binding: FragmentAddBatchBinding
    private lateinit var progressDialog: CustomProgressDialog
    var academicDocumentId:String? = null
    var semesterDocumentId:String? = null
    var classDocumentId:String? = null
    var batchDocumentId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_batch, container, false)
        binding = FragmentAddBatchBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddBatchViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                academicSemTIL.isEnabled=false
                academicClassTIL.isEnabled=false
                academicBatchTIL.isEnabled=false
                btnAddBatch.isEnabled=false
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
                    if(academicClassSelectedItem!=null)
                    {
                        academicClassACTV.setText(academicClassSelectedItem)
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
                    if(academicClassSelectedItem!=null)
                    {
                        academicBatchTIL.isEnabled=true
                    }
                    if(batchET.text.toString()!="")
                    {
                        btnAddBatch.isEnabled=true
                    }
                }
                if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null)
                {
                    loadAcademicSemACTV()
                }

                loadAcademicClassACTV()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {


                actAcademicYear.setOnItemClickListener { parent, view, position, id ->
                    academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                    tlAcademicType.isEnabled=false
                    academicSemTIL.isEnabled=false
                    academicClassTIL.isEnabled=false
                    academicBatchTIL.isEnabled=false
                    btnAddBatch.isEnabled=false
                    clearactAcademicType()
                    clearAcademicSemACTV()
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()

                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        val evenExists =
                            isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                        if (evenExists) {
                            academicTypeItemList.add(AcademicType.EVEN.name)
                        }

                        val oddExists =
                            isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
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
                actAcademicType.setOnItemClickListener { parent, view, position, id ->
                    academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                    academicSemTIL.isEnabled=false
                    academicClassTIL.isEnabled=false
                    academicBatchTIL.isEnabled=false
                    btnAddBatch.isEnabled=false
                    clearAcademicSemACTV()
                    loadAcademicSemACTV()
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()
                    academicSemTIL.isEnabled=true
                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    academicSemSelectedItem = parent.getItemAtPosition(position).toString()

                    academicClassTIL.isEnabled=false
                    academicBatchTIL.isEnabled=false
                    btnAddBatch.isEnabled=false
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()
                    loadAcademicClassACTV()
                    academicClassTIL.isEnabled=true
                }
                academicClassACTV.setOnItemClickListener { parent, view, position, id ->
                    academicClassSelectedItem = parent.getItemAtPosition(position).toString()
                    academicBatchTIL.isEnabled=false
                    btnAddBatch.isEnabled=false
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()
                    initRecyclerView()
                    academicBatchTIL.isEnabled=true
                }

                batchET.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        btnAddBatch.isEnabled = s.toString() != ""
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                btnAddBatch.setOnClickListener {
                    if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null && academicSemSelectedItem!=null && academicClassSelectedItem!=null && batchET.text!!.toString().isNotBlank() && batchET.text!!.toString().isNotEmpty())
                    {
                        academicDocumentId = "${academicYearSelectedItem}_${academicTypeSelectedItem}"
                        semesterDocumentId = academicSemSelectedItem
                        classDocumentId = academicClassSelectedItem
                        batchDocumentId = batchET.text.toString()
                        if(academicDocumentId!=null && semesterDocumentId!=null && classDocumentId!=null && batchDocumentId!=null)
                        {
                            isBatchDocumentExists(academicDocumentId!!,
                                semesterDocumentId!!, classDocumentId!!, batchDocumentId!!
                            ){
                                if(!it)
                                {
                                    val job = MainScope().launch(Dispatchers.IO) {
                                        val inputHashMap = hashMapOf(
                                            "batch" to batchET.text.toString()
                                        )
                                        insertBatchDocument(academicDocumentId!!,
                                            semesterDocumentId!!,
                                            classDocumentId!!, batchDocumentId!!,inputHashMap,{
                                            academicBatchList.add(batchET.text.toString())
                                            academicBatchAdapter.notifyItemInserted(academicBatchAdapter.itemCount)
                                            Toast.makeText(requireContext(),"Batch Added", Toast.LENGTH_SHORT).show()
                                            batchET.setText("")
                                        },{

                                        })
                                    }
                                    runBlocking {
                                        job.join()
                                    }
                                }
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
                progressDialog.start("Loading Batches...")
                MainScope().launch(Dispatchers.IO)
                {
                    academicBatchList.clear()
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())

                    academicDocumentId = "${academicYearSelectedItem}_${academicTypeSelectedItem}"
                    semesterDocumentId = academicSemSelectedItem
                    classDocumentId = academicClassSelectedItem
                    if(academicDocumentId!=null && semesterDocumentId != null && classDocumentId!=null)
                    {
                        val documents = BatchPojo.getAllBatchDocuments(academicDocumentId!!,semesterDocumentId!!, classDocumentId!!)
                        for (document in documents) {
                            academicBatchList.add(document.id)
                        }
                    }

                    withContext(Dispatchers.Main)
                    {
                        academicBatchAdapter = AddBatchAdapter(academicBatchList)
                        recyclerView.adapter = academicBatchAdapter
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

    private fun clearAcademicClassACTV()
    {
        binding.apply {
            viewModel.apply {
                academicClassSelectedItem=null
                academicClassItemList.clear()
                academicClassItemAdapter.notifyDataSetChanged()
                academicClassACTV.setText("")
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

    private fun loadactAcademicYear()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO){
                    academicYearItemList.clear()
                    val documents = AcademicPojo.getAllAcademicDocuments()
                    for (document in documents) {
                        val academicItem = document.id.split('_')
                        if(!academicYearItemList.contains(academicItem[0]))
                        {
                            academicYearItemList.add(academicItem[0])
                        }
                    }
                    withContext(Dispatchers.Main){
                        academicYearItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearItemList)
                        actAcademicYear.setAdapter(academicYearItemAdapter)
                    }
                }

            }
        }
    }

    private fun loadactAcademicType()
    {
        binding.apply {
            viewModel.apply {
                tlAcademicType.isEnabled=false
                academicTypeItemList.clear()
                val job = lifecycleScope.launch(Dispatchers.Main) {
                    val evenExists =
                        isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}")
                    if (evenExists) {
                        academicTypeItemList.add(AcademicType.EVEN.name)
                    }

                    val oddExists =
                        isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}")
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
                MainScope().launch(Dispatchers.IO)
                {
                    academicSemItemList.clear()
                    academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                    if(academicDocumentId!=null)
                    {
                        val documents = SemesterPojo.getAllSemesterDocuments(academicDocumentId!!)
                        for (document in documents) {
                            academicSemItemList.add(document.id.toInt())
                        }
                    }
                    withContext(Dispatchers.Main)
                    {
                        academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                        academicSemACTV.setAdapter(academicSemItemAdapter)
                    }
                }
            }
        }
    }

    private fun loadAcademicClassACTV()
    {
        binding.apply {
            viewModel.apply {
                MainScope().launch(Dispatchers.IO)
                {

                    academicClassItemList.clear()
                    academicDocumentId = "${academicYearSelectedItem}_$academicTypeSelectedItem"
                    semesterDocumentId = academicSemSelectedItem
                    if(academicDocumentId!=null && semesterDocumentId!=null)
                    {
                        val documents = ClassPojo.getAllClassDocuments(academicDocumentId!!,semesterDocumentId!!)
                        for (document in documents) {
                            academicClassItemList.add(document.id)
                        }
                    }
                    withContext(Dispatchers.Main)
                    {
                        academicClassItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicClassItemList)
                        academicClassACTV.setAdapter(academicClassItemAdapter)
                    }
                }
            }
        }
    }


}