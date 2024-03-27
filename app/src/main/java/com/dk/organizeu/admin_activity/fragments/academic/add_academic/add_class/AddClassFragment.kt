package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_class

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.adapter.AddClassAdapter
import com.dk.organizeu.admin_activity.adapter.AddSemAdapter
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_sem.AddSemFragment
import com.dk.organizeu.admin_activity.util.UtilFunction
import com.dk.organizeu.databinding.FragmentAddClassBinding
import com.google.firebase.firestore.FirebaseFirestore

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_class, container, false)
        binding = FragmentAddClassBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddClassViewModel::class.java]
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
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
                    clearAcademicTypeACTV()
                    clearAcademicSemACTV()
                    academicClassList.clear()
                    academicClassAdapter.notifyDataSetChanged()

                    isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}"){
                        if(it)
                        {
                            academicTypeItemList.add(AcademicType.EVEN.name)
                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            academicTypeACTV.setAdapter(academicTypeItemAdapter)
                        }
                    }
                    isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}"){
                        if(it)
                        {
                            academicTypeItemList.add(AcademicType.ODD.name)
                            academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                            academicTypeACTV.setAdapter(academicTypeItemAdapter)
                        }
                    }
                }
                academicTypeACTV.setOnItemClickListener { parent, view, position, id ->
                    academicTypeSelectedItem = parent.getItemAtPosition(position).toString()
                    clearAcademicSemACTV()
                    loadAcademicSemACTV()
                    academicClassList.clear()
                    academicClassAdapter.notifyDataSetChanged()
                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                    academicClassList.clear()
                    academicClassAdapter.notifyDataSetChanged()
                    initRecyclerView()
                }

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
                academicTypeItemList.clear()
                isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.EVEN.name}"){
                    if(it)
                    {
                        academicTypeItemList.add(AcademicType.EVEN.name)
                        academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                        academicTypeACTV.setAdapter(academicTypeItemAdapter)
                    }
                }
                isAcademicDocumentExists("${academicYearSelectedItem!!}_${AcademicType.ODD.name}"){
                    if(it)
                    {
                        academicTypeItemList.add(AcademicType.ODD.name)
                        academicTypeItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypeItemList)
                        academicTypeACTV.setAdapter(academicTypeItemAdapter)
                    }
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

    private fun isAcademicDocumentExists(academicDocumentId: String, callback: (Boolean) -> Unit) {
        db.collection("academic")
            .document(academicDocumentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                callback(documentSnapshot.exists())
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error checking document existence", exception)
                callback(false) // Assume document doesn't exist if there's an error
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