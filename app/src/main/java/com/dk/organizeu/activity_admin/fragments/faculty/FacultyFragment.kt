package com.dk.organizeu.activity_admin.fragments.faculty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.adapter.FacultyAdapter
import com.dk.organizeu.databinding.FragmentFacultyBinding
import com.dk.organizeu.firebase.key_mapping.FacultyCollection
import com.dk.organizeu.repository.FacultyRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.*

class FacultyFragment : Fragment() {

    companion object {
        fun newInstance() = FacultyFragment()
        const val TAG = "OrganizeU-FacultyFragment"
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
                try {
                    initRecyclerView()
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

                swipeRefresh.setOnRefreshListener {
                    initRecyclerView()
                    swipeRefresh.isRefreshing=false
                }

                btnAddFaculty.setOnClickListener {
                    try {
                        val txtFacultyName = etFacultyName.text.toString()
                        tlFacultyName.error = null
                        if(txtFacultyName!="" && txtFacultyName.matches("^[a-zA-Z_-]{2,20}$".toRegex()))
                        {
                            val inputHashMap = hashMapOf(FacultyCollection.FACULTY_NAME.displayName to txtFacultyName)
                            FacultyRepository.insertFacultyDocument(txtFacultyName,inputHashMap,{
                                try {
                                    facultyList.add(txtFacultyName)
                                    facultyAdapter.notifyItemInserted(facultyAdapter.itemCount)
                                    etFacultyName.setText("")
                                    Toast.makeText(requireContext(),"Faculty Added", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e(TAG,e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            },{
                                Log.e(TAG,it.message.toString())
                                requireContext().unexpectedErrorMessagePrint(it)
                            })
                        }
                        else{
                            tlFacultyName.error = "Faculty name only have alphabet, -, _ with 2-20 length"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }
            }
        }
    }

    fun showToast(message: String)
    {
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                try {
                    showProgressBar(rvFaculty,progressBar)
                    MainScope().launch(Dispatchers.IO){
                        try {
                            facultyList.clear()
                            val documents = FacultyRepository.getAllFacultyDocuments()
                            for (document in documents) {
                                facultyList.add(document.id)
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    facultyAdapter = FacultyAdapter(facultyList)
                                    rvFaculty.layoutManager = LinearLayoutManager(requireContext())
                                    rvFaculty.adapter = facultyAdapter
                                    delay(500)
                                    hideProgressBar(rvFaculty,progressBar)
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
}