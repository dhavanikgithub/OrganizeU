package com.dk.organizeu.activity_admin.fragments.subjects

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.adapter.SubjectAdapter
import com.dk.organizeu.activity_admin.dialog.AddSubjectDialog
import com.dk.organizeu.databinding.FragmentSubjectsBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class SubjectsFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = SubjectsFragment()
        const val TAG = "OrganizeU-SubjectsFragment"
    }

    private lateinit var viewModel: SubjectsViewModel
    private lateinit var binding: FragmentSubjectsBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_subjects, container, false)
        binding = FragmentSubjectsBinding.bind(view)
        viewModel =ViewModelProvider(this)[SubjectsViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db= FirebaseFirestore.getInstance()
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
                btnAddSubject.setOnClickListener {
                    val dialogFragment = AddSubjectDialog()
                    try {
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                try {
                    showProgressBar(rvSubjects,progressBar)
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            subjectPojoList.clear()
                            val documents = SubjectRepository.getAllSubjectDocuments()
                            for(document in documents)
                            {
                                val subjectItem = SubjectRepository.subjectDocumentToSubjectObj(document)
                                subjectPojoList.add(subjectItem)
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    subjectAdapter = SubjectAdapter(subjectPojoList,this@SubjectsFragment)
                                    rvSubjects.layoutManager = LinearLayoutManager(requireContext())
                                    rvSubjects.adapter = subjectAdapter
                                    delay(500)
                                    hideProgressBar(rvSubjects,progressBar)
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

    override fun onAdded(documentId: String,documentData: HashMap<String,String>) {
        binding.apply {
            viewModel.apply {
                try {
                    val subjectItem = SubjectRepository.subjectDocumentToSubjectObj(documentId,documentData)
                    subjectPojoList.add(subjectItem)
                    subjectAdapter.notifyItemInserted(subjectAdapter.itemCount)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onClick(position: Int) {}

    override fun onDeleteClick(position: Int) {}
}