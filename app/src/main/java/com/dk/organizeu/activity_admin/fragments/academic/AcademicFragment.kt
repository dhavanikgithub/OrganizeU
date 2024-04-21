package com.dk.organizeu.activity_admin.fragments.academic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.databinding.FragmentAcademicBinding
import com.dk.organizeu.adapter.AcademicAdapter
import com.dk.organizeu.activity_admin.dialog.AddAcademicDialog
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.*

class AcademicFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = AcademicFragment()
        const val TAG = "OrganizeU-AcademicFragment"
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
                try {
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    progressDialog = CustomProgressDialog(requireContext())
                    initRecyclerView()
                    btnAddAcademic.setOnClickListener {
                        val dialogFragment = AddAcademicDialog()
                        dialogFragment.show(childFragmentManager, "customDialog")
                    }
                    academicList.observe(viewLifecycleOwner){
                        academicAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

                swipeRefresh.setOnRefreshListener {
                    initRecyclerView()
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                try {
                    showProgressBar(rvAcademic,progressBar)
                    academicList.value!!.clear()
                    academicAdapter = AcademicAdapter(academicList.value!!,this@AcademicFragment)
                    rvAcademic.layoutManager = LinearLayoutManager(requireContext())
                    rvAcademic.adapter = academicAdapter
                    MainScope().launch(Dispatchers.IO)
                    {
                        try {
                            AcademicRepository.academicCollectionRef().addSnapshotListener { value, error ->
                                try {
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
                                    hideProgressBar(rvAcademic,progressBar)
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
                    val academicItem = documentId.split('_')
                    academicList.value!!.add(AcademicPojo("${academicItem[0]}", "${academicItem[1]}"))
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onClick(position: Int) {
        binding.apply {
            viewModel.apply {
                try {
                    val bundle = Bundle().apply {
                        putString("academic_year", "${academicList.value!![position].academic}")
                        putString("academic_type", "${academicList.value!![position].sem}")
                    }
                    findNavController().navigate(R.id.addAcademicFragment,bundle)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }

    }

    override fun onDeleteClick(position: Int) {

    }
}