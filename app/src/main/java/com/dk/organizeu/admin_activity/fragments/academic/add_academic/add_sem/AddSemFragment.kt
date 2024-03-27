package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_sem

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.adapter.AcademicAdapter
import com.dk.organizeu.admin_activity.adapter.AddSemAdapter
import com.dk.organizeu.admin_activity.data_class.AcademicItem
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicFragment
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.AddAcademicViewModel
import com.dk.organizeu.admin_activity.util.UtilFunction
import com.dk.organizeu.databinding.FragmentAddSemBinding
import com.google.firebase.firestore.FirebaseFirestore

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
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_sem, container, false)
        binding = FragmentAddSemBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddSemViewModel::class.java]
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
                    loadAcademicYearACTV()
                    loadAcademicTypeACTV()
                    initRecyclerView()

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
                    academicSemList.clear()
                    academicSemAdapter.notifyDataSetChanged()

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
                    academicSemList.clear()
                    academicSemAdapter.notifyDataSetChanged()
                    db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem").collection("semester")
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                academicSemList.add(document.id)
                            }
                            academicSemAdapter.notifyDataSetChanged()
                            loadAcademicSemACTV()
                        }
                        .addOnFailureListener { exception ->

                        }


                }

                academicSemACTV.setOnItemClickListener { parent, view, position, id ->
                    academicSemSelectedItem = parent.getItemAtPosition(position).toString()
                }
                btnAddSem.setOnClickListener {
                    if(academicTypeSelectedItem!=null && academicYearSelectedItem!=null && academicSemSelectedItem!=null)
                    {
                        db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem").collection("semester")
                            .document(academicSemSelectedItem!!)
                            .set(hashMapOf(
                                "sem" to academicSemSelectedItem
                            ))
                            .addOnSuccessListener {
                                academicSemList.add(academicSemSelectedItem!!)
                                academicSemAdapter.notifyItemInserted(academicSemAdapter.itemCount)
                                clearAcademicSemACTV()
                                loadAcademicSemACTV()
                                Toast.makeText(requireContext(),"Sem Added",Toast.LENGTH_SHORT).show()
                            }
                    }
                }

            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                academicSemList.clear()
                academicSemLayoutManager = LinearLayoutManager(requireContext())
                recyclerView.layoutManager = academicSemLayoutManager
                db.collection("academic").document("${academicYearSelectedItem}_$academicTypeSelectedItem").collection("semester")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            academicSemList.add(document.id)
                        }
                        academicSemAdapter = AddSemAdapter(academicSemList)
                        recyclerView.adapter = academicSemAdapter
                        loadAcademicSemACTV()
                    }
                    .addOnFailureListener { exception ->

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
}