package com.dk.organizeu.admin_activity.fragments.academic.add_academic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.AdminActivity
import com.dk.organizeu.admin_activity.adapter.TabAdapter
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_batch.AddBatchFragment
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_class.AddClassFragment
import com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_sem.AddSemFragment
import com.dk.organizeu.databinding.FragmentAddAcademicBinding
import com.dk.organizeu.utils.CustomProgressDialog


class AddAcademicFragment : Fragment() {

    companion object {
        fun newInstance() = AddAcademicFragment()
        var academicYear:String?=null
        var academicType:String?=null
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
                (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                val arguments = requireArguments()
                academicYear = arguments.getString("academic_year",null)
                academicType = arguments.getString("academic_type",null)
                AddAcademicFragment.academicYear=academicYear
                AddAcademicFragment.academicType=academicType
                loadTab()
                loadTabFragment()
            }
        }
    }

    private fun loadTab()
    {
        binding.apply {
            viewModel.apply {
                val tab1 = tabLayout.newTab().setText("Sem")
                val tab2 = tabLayout.newTab().setText("Class")
                val tab3 = tabLayout.newTab().setText("Batch")
                tabLayout.addTab(tab1)
                tabLayout.addTab(tab2)
                tabLayout.addTab(tab3)
            }
        }
    }

    private fun loadTabFragment(){
        binding.apply {
            viewModel.apply {
                val adapter = TabAdapter(childFragmentManager)
                adapter.addFragment(AddSemFragment.newInstance(viewModel), "Sem")
                adapter.addFragment(AddClassFragment.newInstance(viewModel), "Class")
                adapter.addFragment(AddBatchFragment.newInstance(), "Batch")
                viewPager.adapter = adapter
                tabLayout.setupWithViewPager(viewPager)
            }
        }
    }
}