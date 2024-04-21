package com.dk.organizeu.activity_admin.fragments.academic.add_academic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.adapter.AcademicDetailsTabAdapter
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_batch.AddBatchFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_class.AddClassFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem.AddSemFragment
import com.dk.organizeu.databinding.FragmentAddAcademicBinding
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint


class AddAcademicFragment : Fragment() {

    companion object {
        fun newInstance() = AddAcademicFragment()
        var academicYear:String?=null
        var academicType:String?=null
        const val TAG = "OrganizeU-AddAcademicFragment"
    }

    private lateinit var viewModel: AddAcademicViewModel

    private lateinit var binding: FragmentAddAcademicBinding
    private lateinit var progressDialog: CustomProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(com.dk.organizeu.R.layout.fragment_add_academic, container, false)
        binding = FragmentAddAcademicBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddAcademicViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
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
                    val arguments = requireArguments()
                    academicYear = arguments.getString("academic_year",null)
                    academicType = arguments.getString("academic_type",null)
                    AddAcademicFragment.academicYear=academicYear
                    AddAcademicFragment.academicType=academicType
                    loadTab()
                    loadTabFragment()
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun loadTab()
    {
        binding.apply {
            viewModel.apply {
                try {
                    val tab1 = tbLayoutAcademicData.newTab().setText("Sem")
                    val tab2 = tbLayoutAcademicData.newTab().setText("Class")
                    val tab3 = tbLayoutAcademicData.newTab().setText("Batch")
                    tbLayoutAcademicData.addTab(tab1)
                    tbLayoutAcademicData.addTab(tab2)
                    tbLayoutAcademicData.addTab(tab3)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    private fun loadTabFragment(){
        binding.apply {
            viewModel.apply {
                try {
                    val adapter = AcademicDetailsTabAdapter(childFragmentManager)
                    adapter.addFragment(AddSemFragment.newInstance(viewModel), "Sem")
                    adapter.addFragment(AddClassFragment.newInstance(viewModel), "Class")
                    adapter.addFragment(AddBatchFragment.newInstance(), "Batch")
                    vpAcademicData.adapter = adapter
                    tbLayoutAcademicData.setupWithViewPager(vpAcademicData)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }
}