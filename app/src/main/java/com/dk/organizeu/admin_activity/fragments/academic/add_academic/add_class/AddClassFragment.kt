package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_class

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.dk.organizeu.admin_activity.adapter.AddClassAdapter
import com.dk.organizeu.admin_activity.adapter.AddSemAdapter
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_sem.AddSemFragment
import com.dk.organizeu.admin_activity.util.UtilFunction
import com.dk.organizeu.admin_activity.util.UtilFunction.Companion.isAcademicDocumentExists
import com.dk.organizeu.databinding.FragmentAddClassBinding
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddClassFragment : Fragment() {

    companion object {
        var viewModel2: AddAcademicViewModel?=null
        fun newInstance(viewModel2: AddAcademicViewModel): AddClassFragment {
            AddClassFragment.viewModel2=viewModel2
            return AddClassFragment()
        }
    }

    private lateinit var viewModel: AddClassViewModel
    private lateinit var binding: FragmentAddClassBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var progressDialog: CustomProgressDialog

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

                    academicYearACTV.setText(academicYearSelectedItem)
                    academicTypeACTV.setText(academicTypeSelectedItem)
                    if(academicSemSelectedItem!=null)
                    {
                        academicSemACTV.setText(academicSemSelectedItem)
                    }
                    loadAcademicYearACTV()
                    loadAcademicTypeACTV()
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
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {

                academicYearACTV.setOnItemClickListener { parent, view, position, id ->
                    academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                    academicTypeTIL.isEnabled=false
                    academicSemTIL.isEnabled=false
                    academicClassTIL.isEnabled=false
                    btnAddClass.isEnabled=false
                    clearAcademicTypeACTV()
                    clearAcademicSemACTV()
                    academicClassList.clear()
                    academicClassAdapter.notifyDataSetChanged()


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
                        academicTypeACTV.setAdapter(academicTypeItemAdapter)
                        academicTypeTIL.isEnabled=true
                    }

                    MainScope().launch{
                        job.join()
                    }
                }
                academicTypeACTV.setOnItemClickListener { parent, view, position, id ->
                    academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                    academicSemTIL.isEnabled=false
                    academicClassTIL.isEnabled=false
                    btnAddClass.isEnabled=false
                    clearAcademicSemACTV()
                    loadAcademicSemACTV()
                    academicClassList.clear()
                    academicClassAdapter.notifyDataSetChanged()
                    academicSemTIL.isEnabled=true
                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                    academicClassTIL.isEnabled=false
                    btnAddClass.isEnabled=false
                    academicClassList.clear()
                    academicClassAdapter.notifyDataSetChanged()
                    initRecyclerView()
                    academicClassTIL.isEnabled=true
                }

                classET.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        btnAddClass.isEnabled = s.toString() != ""
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                btnAddClass.setOnClickListener {
                    if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null && academicSemSelectedItem!=null && classET.text!!.toString().isNotBlank() && classET.text!!.toString().isNotEmpty())
                    {
                        isClassDocumentExists(classET.text.toString()){
                            if(!it)
                            {
                                db.collection("academic").document("${academicYearSelectedItem}_${academicTypeSelectedItem}")
                                    .collection("semester")
                                    .document("$academicSemSelectedItem")
                                    .collection("class")
                                    .document(classET.text.toString())
                                    .set(hashMapOf(
                                        "class" to classET.text.toString()
                                    )).addOnSuccessListener {
                                        academicClassList.add(classET.text.toString())
                                        academicClassAdapter.notifyItemInserted(academicClassAdapter.itemCount)
                                        classET.setText("")
                                        Toast.makeText(requireContext(),"Class Added", Toast.LENGTH_SHORT).show()
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
                progressDialog.start("Loading Classes...")
                academicClassList.clear()
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem")
                    .collection("semester")
                    .document(academicSemSelectedItem.toString())
                    .collection("class")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            academicClassList.add(document.id)
                        }
                        academicClassAdapter = AddClassAdapter(academicClassList)
                        recyclerView.adapter = academicClassAdapter
                        progressDialog.stop()
                    }
                    .addOnFailureListener {
                        progressDialog.stop()
                    }
            }
        }
    }


    private fun clearAcademicTypeACTV()
    {
        binding.apply {
            viewModel.apply {
                academicTypeSelectedItem=null
                academicTypeItemList.clear()
                academicTypeItemAdapter.notifyDataSetChanged()
                academicTypeACTV.setText("")
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

    private fun loadAcademicYearACTV()
    {
        binding.apply {
            viewModel.apply {
                academicYearItemList.clear()
                db.collection("academic").get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        val academicItem = document.id.split('_')
                        if(!academicYearItemList.contains(academicItem[0]))
                        {
                            academicYearItemList.add(academicItem[0])
                        }
                    }
                    academicYearItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYearItemList)
                    academicYearACTV.setAdapter(academicYearItemAdapter)
                }
            }
        }
    }

    private fun loadAcademicTypeACTV()
    {
        binding.apply {
            viewModel.apply {
                academicTypeTIL.isEnabled=false
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
                    academicTypeACTV.setAdapter(academicTypeItemAdapter)
                    academicTypeTIL.isEnabled=true
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
                db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem")
                    .collection("semester")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (document in documents) {
                            academicSemItemList.add(document.id.toInt())
                        }
                    }

                academicSemItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicSemItemList)
                academicSemACTV.setAdapter(academicSemItemAdapter)
            }
        }
    }


    private fun isClassDocumentExists(academicClassDocumentId:String, callback: (Boolean) -> Unit) {
        binding.apply {
            viewModel.apply {
                db.collection("academic")
                    .document("${academicYearSelectedItem}_${academicTypeSelectedItem}")
                    .collection("semester")
                    .document("$academicSemSelectedItem")
                    .collection("class")
                    .document(academicClassDocumentId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        callback(documentSnapshot.exists())
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error checking document existence", exception)
                        callback(false) // Assume document doesn't exist if there's an error
                    }
            }
        }
    }
}