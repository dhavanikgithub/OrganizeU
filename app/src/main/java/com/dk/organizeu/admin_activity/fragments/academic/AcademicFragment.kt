package com.dk.organizeu.admin_activity.fragments.academic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.AdminActivity
import com.dk.organizeu.admin_activity.data_class.AcademicItem
import com.dk.organizeu.databinding.FragmentAcademicBinding
import com.dk.organizeu.admin_activity.adapter.AcademicAdapter
import com.dk.organizeu.admin_activity.dialog_box.AddAcademicDialog
import com.dk.organizeu.admin_activity.listener.AcademicAddListener
import com.dk.organizeu.admin_activity.listener.OnAcademicItemClickListener
import com.dk.organizeu.model.AcademicPojo
import com.dk.organizeu.student_activity.StudentActivity
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AcademicFragment : Fragment(), AcademicAddListener, OnAcademicItemClickListener {

    companion object {
        fun newInstance() = AcademicFragment()
    }

    private lateinit var viewModel: AcademicViewModel
    private lateinit var binding: FragmentAcademicBinding
    private val db = FirebaseFirestore.getInstance()
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
//                    findNavController().navigate(R.id.addAcademicFragment)
                }

            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            viewModel.apply {
                progressDialog.start("Loading Academic Data....")
                rvAcademic.layoutManager = LinearLayoutManager(requireContext())
                MainScope().launch(Dispatchers.IO)
                {
                    academicList.clear()

                    val documents = AcademicPojo.getAllAcademicDocuments()
                    for (document in documents) {
                        // Get the document ID
                        val documentId = document.id
                        val academicItem = documentId.split('_')

                        academicList.add(AcademicItem("${academicItem[0]}", "${academicItem[1]}"))

                    }
                    withContext(Dispatchers.Main)
                    {
                        academicAdapter = AcademicAdapter(academicList,this@AcademicFragment)
                        rvAcademic.adapter = academicAdapter
                        progressDialog.stop()
                    }
                }
            }
        }
    }


    override fun onAcademicAdded(academicDocumentId: String) {
        binding.apply {
            viewModel.apply {
                val academicItem = academicDocumentId.split('_')
                academicList.add(AcademicItem("${academicItem[0]}", "${academicItem[1]}"))
                academicAdapter.notifyItemInserted(academicAdapter.itemCount)
            }
        }
    }

    override fun onItemClick(position: Int) {
        binding.apply {
            viewModel.apply {
//                Toast.makeText(requireContext(),position.toString(),Toast.LENGTH_SHORT).show()
                val bundle = Bundle().apply {
                    putString("academic_year", "${academicList[position].academic}")
                    putString("academic_type", "${academicList[position].sem}")
                    // Add other arguments as needed
                }
                findNavController().navigate(R.id.addAcademicFragment,bundle)
            }
        }

    }
}