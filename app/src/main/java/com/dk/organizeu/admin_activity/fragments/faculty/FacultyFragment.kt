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
import com.dk.organizeu.model.FacultyPojo
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FacultyFragment : Fragment() {

    companion object {
        fun newInstance() = FacultyFragment()
    }

    private lateinit var viewModel: FacultyViewModel
    private lateinit var binding: FragmentFacultyBinding
    private lateinit var progressDialog: CustomProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view  = inflater.inflate(R.layout.fragment_faculty, container, false)
        binding = FragmentFacultyBinding.bind(view)
        viewModel = ViewModelProvider(this)[FacultyViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                initRecyclerView()

                btnAddFaculty.setOnClickListener {
                    val txtFacultyName = etFacultyName.text.toString()
                    if(txtFacultyName!="")
                    {
                        val inputHashMap = hashMapOf("name" to txtFacultyName)
                        FacultyPojo.insertFacultyDocument(txtFacultyName,inputHashMap,{
                            facultyList.add(txtFacultyName)
                            facultyAdapter.notifyItemInserted(facultyAdapter.itemCount)
                            etFacultyName.setText("")
                            Toast.makeText(requireContext(),"Faculty Added", Toast.LENGTH_SHORT).show()
                        },{

                        })
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
                MainScope().launch(Dispatchers.IO){
                    facultyList.clear()
                    rvFaculty.layoutManager = LinearLayoutManager(requireContext())
                    val documents = FacultyPojo.getAllFacultyDocuments()
                    for (document in documents) {
                        facultyList.add(document.id)
                    }
                    withContext(Dispatchers.Main)
                    {
                        facultyAdapter = FacultyAdapter(facultyList)
                        rvFaculty.adapter = facultyAdapter
                        progressDialog.stop()
                    }
                }
            }
        }
    }
}