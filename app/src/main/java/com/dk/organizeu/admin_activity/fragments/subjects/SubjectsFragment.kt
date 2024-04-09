package com.dk.organizeu.admin_activity.fragments.subjects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.adapter.SubjectAdapter
import com.dk.organizeu.admin_activity.dialog_box.AddSubjectDialog
import com.dk.organizeu.admin_activity.listener.OnSubjectItemClickListener
import com.dk.organizeu.admin_activity.listener.SubjectAddListener
import com.dk.organizeu.databinding.FragmentSubjectsBinding
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubjectsFragment : Fragment(), SubjectAddListener, OnSubjectItemClickListener {

    companion object {
        fun newInstance() = SubjectsFragment()
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
                initRecyclerView()

                btnAddSubject.setOnClickListener {
                    val dialogFragment = AddSubjectDialog()
                    dialogFragment.show(childFragmentManager, "customDialog")
                }
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                rvSubjects.layoutManager = LinearLayoutManager(requireContext())
                MainScope().launch(Dispatchers.IO)
                {
                    subjectPojoList.clear()
                    val documents = SubjectRepository.getAllSubjectDocuments()
                    for(document in documents)
                    {
                        val subjectItem = SubjectRepository.subjectDocumentToSubjectObj(document)
                        subjectPojoList.add(subjectItem)
                    }
                    withContext(Dispatchers.Main)
                    {
                        subjectAdapter = SubjectAdapter(subjectPojoList,this@SubjectsFragment)
                        rvSubjects.adapter = subjectAdapter
                    }
                }
            }
        }
    }

    override fun onSubjectAdded(subjectData: HashMap<String, String>, subjectDocumentId: String) {
        binding.apply {
            viewModel.apply {
                val subjectItem = SubjectRepository.subjectDocumentToSubjectObj(subjectDocumentId,subjectData)
                subjectPojoList.add(subjectItem)
                subjectAdapter.notifyItemInserted(subjectAdapter.itemCount)
            }
        }
    }

    override fun onItemClick(position: Int) {
    }
}