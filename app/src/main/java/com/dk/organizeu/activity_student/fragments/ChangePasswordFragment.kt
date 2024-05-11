package com.dk.organizeu.activity_student.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentChangePasswordBinding
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.repository.StudentRepository
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction.Companion.showToast

class ChangePasswordFragment : Fragment() {

    private lateinit var viewModel: ChangePasswordViewModel
    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[ChangePasswordViewModel::class.java]
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        SharedPreferencesManager.addValue(requireContext(),"activeFragment",R.id.changePasswordFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            viewModel.apply {
                btnChangePassword.setOnClickListener {
                    val password = etPassword.text.toString()
                    val newPassword = etNewPassword.text.toString()
                    val id = SharedPreferencesManager.getString(requireContext(), StudentLocalDBKey.ID.displayName, "")

                    if(password.isNotBlank() && newPassword.isNotBlank())
                    {
                        StudentRepository.changePassword(id,password,newPassword,{isChanged ->
                            if(isChanged)
                            {
                                requireContext().showToast("Password Changed")
                                findNavController().popBackStack()
                            }
                        },{isPasswordMatch ->
                            if(!isPasswordMatch)
                            {
                                requireContext().showToast("Password not match")
                            }
                        })
                    }
                }
            }
        }

    }

}