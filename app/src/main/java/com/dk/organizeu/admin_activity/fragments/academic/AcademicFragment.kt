package com.dk.organizeu.admin_activity.fragments.academic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.AdminActivity
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.databinding.FragmentAcademicBinding
import com.dk.organizeu.adapter.AcademicAdapter
import com.dk.organizeu.admin_activity.dialog.AddAcademicDialog
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class AcademicFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = AcademicFragment()
    }

    private lateinit var viewModel: AcademicViewModel
    private lateinit var binding: FragmentAcademicBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_academic, container, false)
        binding = FragmentAcademicBinding.bind(view)
        viewModel = ViewModelProvider(this)[AcademicViewModel::class.java]
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                progressDialog = CustomProgressDialog(requireContext())
                initRecyclerView()
                btnAddAcademic.setOnClickListener {
                    val dialogFragment = AddAcademicDialog()
                    dialogFragment.show(childFragmentManager, "customDialog")
                }
                academicList.observe(viewLifecycleOwner){
                    academicAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                showProgressBar()
                academicList.value!!.clear()
                academicAdapter = AcademicAdapter(academicList.value!!,this@AcademicFragment)
                rvAcademic.layoutManager = LinearLayoutManager(requireContext())
                rvAcademic.adapter = academicAdapter
                MainScope().launch(Dispatchers.IO)
                {
                    AcademicRepository.academicCollectionRef().addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }

                        if (value != null && !value.isEmpty) {
                            for (change in value.documentChanges) {
                                val documentId = change.document.id
                                val academicItem = documentId.split('_')
                                val academicPojo = AcademicPojo("${academicItem[0]}", "${academicItem[1]}")
                                when(change.type)
                                {
                                    DocumentChange.Type.ADDED -> {
                                        academicList.value!!.add(academicPojo)
                                        academicList.value = academicList.value
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        if(academicList.value!!.contains(academicPojo))
                                        {
                                            val index = academicList.value!!.indexOf(academicPojo)
                                            academicList.value!![index] = academicPojo
                                            academicList.value = academicList.value
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        if(academicList.value!!.contains(academicPojo))
                                        {
                                            academicList.value!!.remove(academicPojo)
                                            academicList.value = academicList.value
                                        }
                                    }
                                }

                            }
                        }

                        hideProgressBar()
                    }
                }
            }
        }
    }

    fun showProgressBar()
    {
        binding.rvAcademic.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar()
    {
        binding.progressBar.visibility = View.GONE
        binding.rvAcademic.visibility = View.VISIBLE
    }


    override fun onAdded(documentId: String,documentData: HashMap<String,String>) {
        binding.apply {
            viewModel.apply {
                val academicItem = documentId.split('_')
                academicList.value!!.add(AcademicPojo("${academicItem[0]}", "${academicItem[1]}"))
            }
        }
    }

    override fun onClick(position: Int) {
        binding.apply {
            viewModel.apply {
//                Toast.makeText(requireContext(),position.toString(),Toast.LENGTH_SHORT).show()
                val bundle = Bundle().apply {
                    putString("academic_year", "${academicList.value!![position].academic}")
                    putString("academic_type", "${academicList.value!![position].sem}")
                    // Add other arguments as needed
                }
                findNavController().navigate(R.id.addAcademicFragment,bundle)
            }
        }

    }

    override fun onDeleteClick(position: Int) {

    }
}