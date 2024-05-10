package com.dk.organizeu.activity_main.fragments.auth.student.login

import android.content.Intent
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
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.pojo.StudentPojo
import com.dk.organizeu.repository.StudentRepository
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentLoginFragment : Fragment() {

    private lateinit var viewModel: StudentLoginViewModel
    private lateinit var binding: FragmentStudentLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_student_login, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[StudentLoginViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtRegister.setOnClickListener {
            gotoRegisterPage()
        }
        if(SharedPreferencesManager.containsKey(requireContext(),StudentLocalDBKey.REMEMBER.displayName))
        {
            binding.etStudentId.setText(SharedPreferencesManager.getString(requireContext(),StudentLocalDBKey.REMEMBER.displayName,""))
            binding.cbRemember.isChecked = true
        }
        binding.txtForgotPassword.setOnClickListener {
            UtilFunction.underConstructionDialog(requireContext())
        }

        binding.btnLogin.setOnClickListener {
            val studentId = binding.etStudentId.text.toString()
            val password = binding.etPassword.text.toString()
            if(studentId.isNotBlank() && password.isNotBlank()){
                signIn(studentId,password)
            }
            else{
                requireContext().showToast("Student Id & Password require to login")
            }
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

    fun signIn(studentId: String, password: String)
    {
        StudentRepository.isMatchLoginDetails(studentId,password,{
            MainScope().launch(Dispatchers.IO)
            {
                if(it)
                {
                    val studentPojo = StudentRepository.getStudentPojoById(studentId)
                    saveStudentLocal(studentPojo)
                    if(binding.cbRemember.isChecked)
                    {
                        setIsRememberLocal(studentPojo.id)
                    }
                    else{
                        if(SharedPreferencesManager.containsKey(requireContext(),StudentLocalDBKey.REMEMBER.displayName))
                        {
                            SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.REMEMBER.displayName)
                        }
                    }
                    gotoStudentActivity()
                }
                else{
                    withContext(Dispatchers.Main)
                    {
                        requireContext().showToast("Invalid Credentials")
                    }
                }
            }
        },{
            requireContext().showToast("Invalid Credentials")
        })
    }

    fun saveStudentLocal(studentPojo: StudentPojo)
    {
        SharedPreferencesManager.addValue(requireContext(), StudentLocalDBKey.ID.displayName,studentPojo.id)
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.NAME.displayName,studentPojo.name)
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.ACADEMIC_YEAR.displayName,studentPojo.academicYear)
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.ACADEMIC_TYPE.displayName,studentPojo.academicType)
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.SEMESTER.displayName,studentPojo.semester)
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.CLASS.displayName,studentPojo.className)
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.BATCH.displayName,studentPojo.batchName)
    }

    fun setIsRememberLocal(studentId: String)
    {
        SharedPreferencesManager.addValue(requireContext(),StudentLocalDBKey.REMEMBER.displayName,studentId)
    }

}