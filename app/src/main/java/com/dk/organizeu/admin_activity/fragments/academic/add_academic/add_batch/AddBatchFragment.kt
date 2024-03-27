package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_batch

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
import com.dk.organizeu.admin_activity.adapter.AddBatchAdapter
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.databinding.FragmentAddBatchBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddBatchFragment : Fragment() {

    companion object {
        fun newInstance() = AddBatchFragment()
    }

    private lateinit var viewModel: AddBatchViewModel
    private lateinit var binding: FragmentAddBatchBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_batch, container, false)
        binding = FragmentAddBatchBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddBatchViewModel::class.java]
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
                    if(academicClassSelectedItem!=null)
                    {
                        academicClassACTV.setText(academicClassSelectedItem)
                    }
                    loadAcademicYearACTV()
                    loadAcademicTypeACTV()
                    initRecyclerView()
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


                academicYearACTV.setOnItemClickListener { parent, view, position, id ->
                    academicYearSelectedItem = parent.getItemAtPosition(position).toString()
                    clearAcademicTypeACTV()
                    clearAcademicSemACTV()
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()

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
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()
                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                    clearAcademicClassACTV()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()
                    loadAcademicClassACTV()
                }
                academicClassACTV.setOnItemClickListener { parent, view, position, id ->
                    academicClassSelectedItem = parent.getItemAtPosition(position).toString()
                    academicBatchList.clear()
                    academicBatchAdapter.notifyDataSetChanged()
                    initRecyclerView()
                }

                btnAddBatch.setOnClickListener {
                    if(academicYearSelectedItem!=null && academicTypeSelectedItem!=null && academicSemSelectedItem!=null && academicClassSelectedItem!=null && batchET.text!!.toString().isNotBlank() && batchET.text!!.toString().isNotEmpty())
                    {
                        isBatchDocumentExists(batchET.text.toString()){
                            if(!it)
                            {
                                db.collection("academic").document("${academicYearSelectedItem}_${academicTypeSelectedItem}")
                                    .collection("semester")
                                    .document("$academicSemSelectedItem")
                                    .collection("class")
                                    .document(academicClassSelectedItem.toString())
                                    .collection("batch")
                                    .document(batchET.text.toString())
                                    .set(hashMapOf(
                                        "batch" to batchET.text.toString()
                                    )).addOnSuccessListener {
                                        academicBatchList.add(batchET.text.toString())
                                        academicBatchAdapter.notifyItemInserted(academicBatchAdapter.itemCount)
                                        batchET.setText("")
                                        Toast.makeText(requireContext(),"Batch Added", Toast.LENGTH_SHORT).show()
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
                academicBatchList.clear()
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem")
                    .collection("semester")
                    .document(academicSemSelectedItem.toString())
                    .collection("class")
                    .document(academicClassSelectedItem.toString())
                    .collection("batch")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            academicBatchList.add(document.id)
                        }
                        academicBatchAdapter = AddBatchAdapter(academicBatchList)
                        recyclerView.adapter = academicBatchAdapter
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

    private fun loadAcademicClassACTV()
    {
        binding.apply {
            viewModel.apply {
                academicClassItemList.clear()
                db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem")
                    .collection("semester")
                    .document(academicSemSelectedItem.toString())
                    .collection("class")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            academicClassItemList.add(document.id)
                        }

                    }
                academicClassItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicClassItemList)
                academicClassACTV.setAdapter(academicClassItemAdapter)
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

    private fun isBatchDocumentExists(academicBatchDocumentId:String, callback: (Boolean) -> Unit) {
        binding.apply {
            viewModel.apply {
                db.collection("academic")
                    .document("${academicYearSelectedItem}_${academicTypeSelectedItem}")
                    .collection("semester")
                    .document("$academicSemSelectedItem")
                    .collection("class")
                    .document(academicClassSelectedItem.toString())
                    .collection("batch")
                    .document(academicBatchDocumentId)
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