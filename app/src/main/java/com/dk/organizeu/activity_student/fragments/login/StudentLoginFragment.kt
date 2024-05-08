package com.dk.organizeu.activity_student.fragments.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.databinding.FragmentStudentLoginBinding
import com.dk.organizeu.utils.Constants.Companion.SHARED_PREFERENCES_NAME
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.auth.FirebaseAuth

class StudentLoginFragment : Fragment() {

    private lateinit var viewModel: StudentLoginViewModel
    private lateinit var binding: FragmentStudentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_student_login, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[StudentLoginViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtRegister.setOnClickListener {
            gotoRegisterPage()
        }
        binding.btnLogin.setOnClickListener {
            gotoStudentActivity()
        }
    }

    private fun gotoRegisterPage()
    {
        findNavController().popBackStack()
        findNavController().navigate(R.id.studentRegisterFragment)
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
}