package com.dk.organizeu.activity_main.fragments.settings

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.activity_main.MainActivity
import com.dk.organizeu.databinding.FragmentSettingsBinding
import com.dk.organizeu.enum_class.AdminLocalDBKey
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.utils.LogoutConfirmDialog
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction.Companion.underConstructionDialog
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var viewModel: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("organizeu_settings", Context.MODE_PRIVATE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.uiMode = sharedPreferences.getInt("uiMode",-1)
        if(viewModel.uiMode == -1)
        {
            // Check if the system default mode is dark
            val systemNightMode =
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    UiModeManager.MODE_NIGHT_YES
                } else {
                    UiModeManager.MODE_NIGHT_NO
                }
            binding.darkModeSwitch.isChecked = systemNightMode == UiModeManager.MODE_NIGHT_YES
        }
        else{
            binding.darkModeSwitch.isChecked = viewModel.uiMode == 0
        }

        viewModel.isStudent = requireArguments().getBoolean("isStudent")

        if(viewModel.isStudent)
        {
            binding.usernameTextView.text = SharedPreferencesManager.getString(requireContext(),StudentLocalDBKey.NAME.displayName)
        }
        else{
            binding.profileCircleImageView.setImageResource(R.drawable.admin_dp_avtar)
            binding.usernameTextView.text = SharedPreferencesManager.getString(requireContext(),AdminLocalDBKey.NAME.displayName)
            binding.switchNotification.visibility = View.GONE
            binding.txtNotificationTitle.visibility = View.GONE
            binding.txtEditProfile.visibility = View.GONE
        }

        binding.layoutEditDetails.setOnClickListener {
            if(viewModel.isStudent)
            {
                findNavController().navigate(R.id.studentProfileFragment)
            }
            else{
                underConstructionDialog(requireContext())
            }
        }
        binding.txtChangePassword.setOnClickListener {
            if(viewModel.isStudent)
            {
                findNavController().navigate(R.id.changePasswordFragment)
            }
            else{
                underConstructionDialog(requireContext())
            }
        }

        binding.txtEditProfile.setOnClickListener {
            if(viewModel.isStudent)
            {
                findNavController().navigate(R.id.studentProfileFragment)
            }
            else{
                underConstructionDialog(requireContext())
            }
        }

        binding.logout.setOnClickListener {
            val logoutDialog = LogoutConfirmDialog(requireContext()).setCancelable(false).build()

            logoutDialog.show({
                if(viewModel.isStudent)
                {
                    clearStudent()
                    gotoMainActivity()
                }
                else{
                    clearAdmin()
                    gotoMainActivity()
                }
            },{
                logoutDialog.dismiss()
            })

        }
    }



    override fun onResume() {
        super.onResume()
        if(viewModel.isStudent)
        {
            SharedPreferencesManager.addValue(requireContext(),"activeFragment",R.id.settingsFragmentStudent)
        }
        else{
            SharedPreferencesManager.addValue(requireContext(),"activeFragment",R.id.settingsFragmentAdmin)
        }
        val editor = sharedPreferences.edit()
        // Toggle night mode
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            editor.putInt("uiMode",if(isChecked) 0 else 1 )
            editor.apply()
        }
    }

    fun clearStudent()
    {
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.ID.displayName)
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.NAME.displayName)
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.ACADEMIC_YEAR.displayName)
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.ACADEMIC_TYPE.displayName)
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.SEMESTER.displayName)
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.CLASS.displayName)
        SharedPreferencesManager.removeValue(requireContext(),StudentLocalDBKey.BATCH.displayName)
    }

    fun clearAdmin()
    {
        SharedPreferencesManager.removeValue(requireContext(),AdminLocalDBKey.ID.displayName)
        SharedPreferencesManager.removeValue(requireContext(),AdminLocalDBKey.NAME.displayName)
    }




    fun gotoMainActivity()
    {
        try {
            // Create an intent to start the StudentActivity
            Intent(requireActivity(), MainActivity::class.java).apply {
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