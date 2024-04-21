package com.dk.organizeu.activity_main.fragments

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.databinding.FragmentSplashBinding
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    companion object {
        fun newInstance() = SplashFragment()
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
            viewModel.apply {
                MainScope().launch {
                    btnAdminScreen.setOnClickListener {
                        gotoAdminActivity()
                    }
                    btnStudentScreen.setOnClickListener {
                        gotoStudentActivity()
                    }
                }
            }
        }
    }

    private fun gotoStudentActivity(){
        try {
            Intent(requireActivity(), StudentActivity::class.java).apply {
                startActivity(this)
            }
            requireActivity().finish()
        } catch (e: Exception) {
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }


    private fun gotoAdminActivity(){
        try {
            Intent(requireActivity(), AdminActivity::class.java).apply {
                startActivity(this)
            }
            requireActivity().finish()
        }
        catch (e:Exception)
        {
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }
}