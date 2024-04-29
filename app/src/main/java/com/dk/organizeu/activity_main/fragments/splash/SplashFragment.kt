package com.dk.organizeu.activity_main.fragments.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.databinding.FragmentSplashBinding
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class SplashFragment : Fragment() {

    companion object {
        fun newInstance() = SplashFragment()
        const val TAG = "OrganizeU-SplashFragment"
    }

    private lateinit var viewModel: SplashViewModel
    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        binding = FragmentSplashBinding.bind(view)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            // Set click listener for admin screen button
            btnAdminScreen.setOnClickListener {
                gotoAdminActivity()
            }

            // Set click listener for student screen button
            btnStudentScreen.setOnClickListener {
                gotoStudentActivity()
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