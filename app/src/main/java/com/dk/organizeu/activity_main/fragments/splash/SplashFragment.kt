package com.dk.organizeu.activity_main.fragments.splash

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentSplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

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

    }

    override fun onResume() {
        super.onResume()
        val isDarkMode = sharedPreferences.getBoolean("isDark",false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        MainScope().launch(Dispatchers.Main)
        {
            viewModel.delayAndNavigate(3000)
            gotoPermissionFragment()
        }

    }

    /**
     * Navigates to the PermissionFragment.
     * If successful, finishes the current fragment.
     */
    private fun gotoPermissionFragment(){
        try {
            findNavController().popBackStack()
            findNavController().navigate(R.id.permissionFragment)
        } catch (e: Exception) {
            // Handle any exceptions and print error messages
            //requireContext().unexpectedErrorMessagePrint(e)
        }
    }

}