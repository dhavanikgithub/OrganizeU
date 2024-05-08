package com.dk.organizeu.activity_main.fragments.user_selection

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.databinding.FragmentUserSelectionBinding
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class UserSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = UserSelectionFragment()
    }

    private val viewModel: UserSelectionViewModel by viewModels()
    private lateinit var binding: FragmentUserSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_selection, container, false)
        binding = DataBindingUtil.bind(view)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            // Set click listener for admin screen button
            btnUserAdmin.setOnClickListener {
                gotoAdminActivity()
            }

            // Set click listener for student screen button
            btnUserStudent.setOnClickListener {
                gotoStudentActivity()
//                gotoStudentLogin()
            }

        }
    }

    /**
     * Navigates to the StudentActivity.
     * If successful, finishes the current activity.
     */
    private fun gotoStudentActivity(){
        try {
            // Create an intent to start the StudentActivity
            Intent(requireActivity(), StudentActivity::class.java).apply {
                // Start the activity
                startActivity(this)
            }
            // Finish the current activity
            requireActivity().finish()
        } catch (e: Exception) {
            // Handle any exceptions and print error messages
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }

    private fun gotoStudentLogin()
    {
        findNavController().popBackStack()
        findNavController().navigate(R.id.studentLoginFragment)
    }

    /**
     * Navigates to the AdminActivity.
     * If successful, finishes the current activity.
     */
    private fun gotoAdminActivity(){
        try {
            // Create an intent to start the AdminActivity
            Intent(requireActivity(), AdminActivity::class.java).apply {
                // Start the activity
                startActivity(this)
            }
            // Finish the current activity
            requireActivity().finish()
        } catch (e:Exception) {
            // Handle any exceptions and print error messages
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }
}