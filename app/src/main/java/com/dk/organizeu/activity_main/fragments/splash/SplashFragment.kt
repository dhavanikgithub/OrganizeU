package com.dk.organizeu.activity_main.fragments.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        binding = FragmentSplashBinding.bind(view)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("organizeu_settings", Context.MODE_PRIVATE)
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

    override fun onResume() {
        super.onResume()
        val isDarkMode = sharedPreferences.getBoolean("isDark",false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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