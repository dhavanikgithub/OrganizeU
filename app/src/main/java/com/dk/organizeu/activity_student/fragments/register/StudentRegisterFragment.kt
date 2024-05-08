package com.dk.organizeu.activity_student.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentStudentRegisterBinding

class StudentRegisterFragment : Fragment() {

    private lateinit var viewModel: StudentRegisterViewModel
    private lateinit var binding: FragmentStudentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_student_register, container, false)
        viewModel = ViewModelProvider(this)[StudentRegisterViewModel::class.java]
        binding = DataBindingUtil.bind(view)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtLogin.setOnClickListener {
            gotoLogin()
        }
    }

    private fun gotoLogin()
    {
        findNavController().popBackStack()
        findNavController().navigate(R.id.studentLoginFragment)
    }
}