package com.dk.organizeu.admin_activity.fragments.faculty

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.adapter.FacultyAdapter
import com.dk.organizeu.databinding.FragmentFacultyBinding
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore

class FacultyFragment : Fragment() {

    companion object {
        fun newInstance() = FacultyFragment()
    }

    private lateinit var viewModel: FacultyViewModel
    private lateinit var binding: FragmentFacultyBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view  = inflater.inflate(R.layout.fragment_faculty, container, false)
        binding = FragmentFacultyBinding.bind(view)
        viewModel = ViewModelProvider(this)[FacultyViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db = FirebaseFirestore.getInstance()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                initRecyclerView()

                btnAddFaculty.setOnClickListener {
                    if(facultyET.text.toString()!="")
                    {
                        db.collection("faculty")
                            .document(facultyET.text.toString())
                            .set(hashMapOf(
                                "name" to facultyET.text.toString()
                            ))
                            .addOnSuccessListener {
                                facultyList.add(facultyET.text.toString())
                                facultyAdapter.notifyItemInserted(facultyAdapter.itemCount)
                                facultyET.setText("")
                                Toast.makeText(requireContext(),"Faculty Added", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                progressDialog.start("Loading Faculty...")
                facultyList.clear()
                facultyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                db.collection("faculty")
                    .get()
                    .addOnSuccessListener {documents ->
                        for (document in documents) {
                            facultyList.add(document.id)
                        }
                        facultyAdapter = FacultyAdapter(facultyList)
                        facultyRecyclerView.adapter = facultyAdapter
                        progressDialog.stop()
                    }
                    .addOnCanceledListener {
                        progressDialog.stop()
                    }
                    .addOnFailureListener {
                        progressDialog.stop()
                    }

            }
        }
    }
}