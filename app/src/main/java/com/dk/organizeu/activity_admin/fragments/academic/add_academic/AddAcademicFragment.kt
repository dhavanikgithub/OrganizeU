package com.dk.organizeu.activity_admin.fragments.academic.add_academic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_batch.AddBatchFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_class.AddClassFragment
import com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem.AddSemFragment
import com.dk.organizeu.adapter.AcademicDetailsTabAdapter
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
        val view = inflater.inflate(R.layout.fragment_add_academic, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[AddAcademicViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            viewModel.apply {
                try {
                    // Select the "Academic" item in the drawer menu of the AdminActivity
                    (activity as? AdminActivity)?.drawerMenuSelect(R.id.nav_academic)
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
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
                    // Retrieve academic year and type from the fragment arguments
                    val arguments = requireArguments()
                    academicYear = arguments.getString("academic_year",null)
                    academicType = arguments.getString("academic_type",null)
                    // Set the retrieved academic year and type for use in the fragment
                    AddAcademicFragment.academicYear=academicYear
                    AddAcademicFragment.academicType=academicType
                    // Load the tab and view pager based on the retrieved data
                    loadTab()
                    loadTabFragment()
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    /**
     * Loads tabs for displaying different categories of academic data.
     * The tabs include 'Sem', 'Class', and 'Batch'.
     */
    private fun loadTab()
    {
        binding.apply {
            viewModel.apply {
                try {
                    // Create tabs for 'Sem', 'Class', and 'Batch'
                    val semTab = tbLayoutAcademicData.newTab().setText("Sem")
                    val classTab = tbLayoutAcademicData.newTab().setText("Class")
                    val batchTab = tbLayoutAcademicData.newTab().setText("Batch")

                    // Add the tabs to the tab layout
                    tbLayoutAcademicData.addTab(semTab)
                    tbLayoutAcademicData.addTab(classTab)
                    tbLayoutAcademicData.addTab(batchTab)
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }

    /**
     * Loads tab fragments for displaying different categories of academic data.
     * This function sets up the ViewPager with the tab layout.
     */
    private fun loadTabFragment(){
        binding.apply {
            viewModel.apply {
                try {
                    // Create a new adapter for managing tab fragments
                    val adapter = AcademicDetailsTabAdapter(childFragmentManager)
                    // Add fragments for 'Sem', 'Class', and 'Batch' to the adapter
                    adapter.addFragment(AddSemFragment.newInstance(viewModel), "Sem")
                    adapter.addFragment(AddClassFragment.newInstance(viewModel), "Class")
                    adapter.addFragment(AddBatchFragment.newInstance(), "Batch")
                    // Set the adapter to the ViewPager
                    vpAcademicData.adapter = adapter
                    // Connect the tab layout with the ViewPager
                    tbLayoutAcademicData.setupWithViewPager(vpAcademicData)
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG, e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                    throw e
                }
            }
        }
    }
}