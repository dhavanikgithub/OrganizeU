package com.dk.organizeu.activity_main.fragments.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.databinding.FragmentSplashBinding
import com.dk.organizeu.enum_class.AdminLocalDBKey
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.utils.PermissionManager
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
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
        MainScope().launch(Dispatchers.Main) {
            Thread.sleep(2000L)
            if(permissionCheck())
            {
                if(isStudentAlreadyLogin())
                {
                    gotoStudentActivity()
                }
                else if(isAdminAlreadyLogin())
                {
                    gotoAdminActivity()
                }
                else
                {
                    gotoUserSelectionPage()
                }
            }
            else{
                findNavController().popBackStack()
                findNavController().navigate(R.id.permissionFragment)
            }
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

    fun isAdminAlreadyLogin():Boolean{
        return SharedPreferencesManager.containsKey(requireContext(), AdminLocalDBKey.ID.displayName)
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
     * Navigate to the UserSelectionFragment.
     * This function pops the back stack to remove any fragments in the navigation stack
     * and then navigates to the UserSelectionFragment.
     */
    private fun gotoUserSelectionPage() {
        try {// Pop the back stack to remove any fragments in the navigation stack
            findNavController().popBackStack()
            // Navigate to the UserSelectionFragment
            findNavController().navigate(R.id.userSelectionFragment)
        } catch (e: Exception) {

        }
    }

    fun isStudentAlreadyLogin():Boolean{
        return SharedPreferencesManager.containsKey(requireContext(), StudentLocalDBKey.ID.displayName)
    }

    private fun permissionCheck(): Boolean {
        binding.apply {
            try {
                var notificationPermission = true // Assume notification permission is granted by default
                val audioSettingPermission =
                    PermissionManager.isAudioSettingsPermissionGranted(requireContext())
                val doNotDisturb =
                    PermissionManager.isDoNotDisturbPermissionGranted(requireContext())


                // Notification Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission =
                        PermissionManager.isNotificationPermissionGranted(requireContext())
                }

                // Return true if all permissions are granted, false otherwise
                return audioSettingPermission && notificationPermission && doNotDisturb
            } catch (e: Exception) {
                // Rethrow any exceptions that occur
                throw e
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
}