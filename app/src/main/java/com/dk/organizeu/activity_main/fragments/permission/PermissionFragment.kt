package com.dk.organizeu.activity_main.fragments.permission

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentPermissionBinding
import com.dk.organizeu.utils.Constants.Companion.AUDIO_SETTING_PERMISSION
import com.dk.organizeu.utils.Constants.Companion.NOTIFICATION_PERMISSION
import com.dk.organizeu.utils.PermissionManager.Companion.appNotificationSettingIntent
import com.dk.organizeu.utils.PermissionManager.Companion.appSettingIntent
import com.dk.organizeu.utils.PermissionManager.Companion.isAudioSettingsPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.isDoNotDisturbPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.isNotificationPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.requestPermission
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class PermissionFragment : Fragment() {

    companion object {
        fun newInstance() = PermissionFragment()
        const val TAG = "OrganizeU-PermissionFragment"

    }

    private lateinit var viewModel: PermissionViewModel
    private lateinit var binding: FragmentPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.fragment_permission, container, false)
        binding = FragmentPermissionBinding.bind(view)
        viewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                    btnStart.isEnabled = permissionCheck()
                    if(btnStart.isEnabled)
                    {
                       gotoSplashFragment()
                    }
                } catch (e: Exception) {
                    requireContext().unexpectedErrorMessagePrint(e)
                }

                btnStart.setOnClickListener {
                    gotoSplashFragment()
                }

                btnAllowNotification.setOnClickListener {
                    btnNotificationClickListener()
                }

                btnAllowDoNotDisturb.setOnClickListener {
                    try {
                        if (txtAllowDoNotDisturb.text.toString().equals(getString(R.string.Allow),true)){
                            if (!isDoNotDisturbPermissionGranted(requireContext())) {
                                appNotificationSettingIntent().apply {
                                    notificationPolicyAccessLauncher.launch(this)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                btnAllowAudio.setOnClickListener {
                    btnAudioClickListener()
                }
            }
        }
    }

    private fun gotoSplashFragment()
    {
        findNavController().popBackStack()
        findNavController().navigate(R.id.splashFragment)
    }

    fun btnNotificationClickListener()
    {
        binding.apply {
            if (txtAllowNotification.text.toString().equals(getString(R.string.Allow),true)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestPermission(requireContext(),NOTIFICATION_PERMISSION,{
                        try {
                            btnStart.isEnabled = permissionCheck()
                        } catch (e: Exception) {
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    },{
                        try {
                            appSettingIntent(requireContext()).apply {
                                settingsActivityResultLauncher.launch(this)
                            }
                        } catch (e: Exception) {
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    })
                }
                else{
                    btnStart.isEnabled = permissionCheck()
                }
            }
        }
    }
    fun btnAudioClickListener()
    {
        binding.apply {
            if (txtAllowAudio.text.toString().equals(getString(R.string.Allow),true)){
                requestPermission(requireContext(), AUDIO_SETTING_PERMISSION,{
                    try {
                        btnStart.isEnabled = permissionCheck()
                    } catch (e: Exception) {
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                },{
                    try {
                        appSettingIntent(requireContext()).apply {
                            settingsActivityResultLauncher.launch(this)
                        }
                    } catch (e: Exception) {
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                })
            }
        }
    }


    private val notificationPolicyAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        try {
            binding.btnStart.isEnabled = permissionCheck()
        } catch (e: Exception) {
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }

    private val settingsActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        try {
            binding.btnStart.isEnabled=permissionCheck()
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            throw e
        }
    }

    private fun setPermissionDone(textView: TextView, layout:LinearLayout)
    {
        textView.text = getString(R.string.Done)
        textView.setTextColor(requireContext().getColor(R.color.colorSecondary))
        layout.setBackgroundColor(requireContext().getColor(R.color.colorSecondaryContainer))
    }

    private fun setPermissionAllow(textView: TextView, layout:LinearLayout)
    {
        textView.text = getString(R.string.Allow)
        textView.setTextColor(requireContext().getColor(R.color.colorPrimary))
        layout.setBackgroundColor(requireContext().getColor(R.color.colorPrimaryContainer))
    }

    private fun permissionCheck():Boolean {
        binding.apply {
            try {
                var notificationPermission = true
                val audioSettingPermission = isAudioSettingsPermissionGranted(requireContext())
                val doNotDisturb = isDoNotDisturbPermissionGranted(requireContext())

                // Do not disturb
                if (doNotDisturb) {
                    setPermissionDone(binding.txtAllowDoNotDisturb,binding.btnAllowDoNotDisturb)
                } else {
                    setPermissionAllow(binding.txtAllowDoNotDisturb,binding.btnAllowDoNotDisturb)
                }

                //Notification Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    notificationPermission = isNotificationPermissionGranted(requireContext())
                    if (notificationPermission) {
                        setPermissionDone(txtAllowNotification,btnAllowNotification)
                    } else {
                        setPermissionAllow(txtAllowNotification,btnAllowNotification)
                    }
                }
                else{
                    setPermissionDone(txtAllowNotification,btnAllowNotification)
                }

                //Audio Permission
                if (audioSettingPermission) {
                    setPermissionDone(txtAllowAudio,btnAllowAudio)
                } else {
                    setPermissionAllow(txtAllowAudio,btnAllowAudio)
                }
                return audioSettingPermission && notificationPermission && doNotDisturb
            } catch (e: Exception) {
                throw  e
            }
        }

    }
}