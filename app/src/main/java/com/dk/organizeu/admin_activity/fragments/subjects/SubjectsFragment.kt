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
import com.dk.organizeu.admin_activity.data_class.Subject
import com.dk.organizeu.admin_activity.dialog_box.AddSubjectDialog
import com.dk.organizeu.admin_activity.listener.OnSubjectItemClickListener
import com.dk.organizeu.admin_activity.listener.SubjectAddListener
import com.dk.organizeu.databinding.FragmentSubjectsBinding
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore

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
                subjectList.clear()
                subjectRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                db.collection("subject")
                    .get()
                    .addOnSuccessListener {documents->
                        for(document in documents)
                        {
                            val subjectItem = Subject(document.id,document.get("code").toString(),document.get("type").toString())
                            subjectList.add(subjectItem)
                        }
                        subjectAdapter = SubjectAdapter(subjectList,this@SubjectsFragment)
                        subjectRecyclerView.adapter = subjectAdapter
                    }
            }
        }
    }

    override fun onSubjectAdded(subjectData: HashMap<String, String>, subjectDocumentId: String) {
        binding.apply {
            viewModel.apply {
                val subjectItem = Subject(subjectDocumentId,subjectData["code"].toString(),subjectData["type"].toString())
                subjectList.add(subjectItem)
                subjectAdapter.notifyItemInserted(subjectAdapter.itemCount)
            }
        }
    }

    override fun onItemClick(position: Int) {
    }
}