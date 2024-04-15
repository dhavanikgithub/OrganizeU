package com.dk.organizeu.main_activity.fragments.permission

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentPermissionBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class PermissionFragment : Fragment() {

    companion object {
        fun newInstance() = PermissionFragment()
        private const val NOTIFICATION_PERMISSION = android.Manifest.permission.POST_NOTIFICATIONS
        private const val AUDIO_SETTING_PERMISSION = android.Manifest.permission.MODIFY_AUDIO_SETTINGS
    }

    private lateinit var viewModel: PermissionViewModel
    private lateinit var binding: FragmentPermissionBinding
    lateinit var notificationManager: NotificationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.fragment_permission, container, false)
        binding = FragmentPermissionBinding.bind(view)
        viewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                btnStart.isEnabled = permissionCheck()
                if(btnStart.isEnabled)
                {
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.splashFragment)
                }
                btnStart.setOnClickListener {
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.splashFragment)
                }
                btnAllowNotification.setOnClickListener {
                    if (txtAllowNotification.text.toString().equals(getString(R.string.Allow),true)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                            Dexter.withContext(requireContext()).withPermission(NOTIFICATION_PERMISSION)
                                .withListener(object : PermissionListener{
                                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                                        txtAllowNotification.text = getString(R.string.Done)
                                        txtAllowNotification.setTextColor(requireContext().getColor(R.color.permissionDoneText))
                                        btnStart.isEnabled = permissionCheck()
                                    }

                                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                                        settingsActivityResultLauncher.launch(intent)
                                    }

                                    override fun onPermissionRationaleShouldBeShown(
                                        p0: PermissionRequest?,
                                        token: PermissionToken?
                                    ) {
                                        token?.continuePermissionRequest()
                                    }

                                })
                                .check()
                        }
                        else{
                            txtAllowNotification.text = getString(R.string.Done)
                            txtAllowNotification.setTextColor(requireContext().getColor(R.color.permissionDoneText))
                            btnStart.isEnabled = permissionCheck()
                        }
                    }
                }

                btnAllowDoNotDisturb.setOnClickListener {
                    if (txtAllowDoNotDisturb.text.toString().equals(getString(R.string.Allow),true)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            notificationPolicyAccessLauncher.launch(intent)
                        }
                    }
                }

                btnAllowAudio.setOnClickListener {
                    if (txtAllowAudio.text.toString().equals(getString(R.string.Allow),true)){
                        Dexter.withContext(requireContext()).withPermission(AUDIO_SETTING_PERMISSION)
                            .withListener(object : PermissionListener{
                                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                                    txtAllowAudio.text = getString(R.string.Done)
                                    txtAllowAudio.setTextColor(requireContext().getColor(R.color.permissionDoneText))
                                    btnStart.isEnabled = permissionCheck()
                                }

                                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                                    settingsActivityResultLauncher.launch(intent)
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                    p0: PermissionRequest?,
                                    token: PermissionToken?
                                ) {
                                    token?.continuePermissionRequest()
                                }

                            })
                            .check()
                    }
                }
            }
        }
    }
    private val notificationPolicyAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        binding.btnStart.isEnabled = permissionCheck()
    }
    fun checkPermissionGranted(context: Context, PERMISSION: String):Boolean{
        return ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    fun permissionCheck():Boolean {
        binding.apply {
            var notificationPermission = true
            val doNotDisturb = notificationManager.isNotificationPolicyAccessGranted
            val audioSettingPermission = checkPermissionGranted(requireContext(),AUDIO_SETTING_PERMISSION)
            if (doNotDisturb) {
                txtAllowDoNotDisturb.text = getString(R.string.Done)
                txtAllowDoNotDisturb.setTextColor(requireContext().getColor(R.color.permissionDoneText))
            } else {
                txtAllowDoNotDisturb.text = getString(R.string.Allow)
                txtAllowDoNotDisturb.setTextColor(requireContext().getColor(R.color.permissionAllowText))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                notificationPermission = checkPermissionGranted(requireContext(),NOTIFICATION_PERMISSION)
                if (notificationPermission) {
                    txtAllowNotification.text = getString(R.string.Done)
                    txtAllowNotification.setTextColor(requireContext().getColor(R.color.permissionDoneText))
                } else {
                    txtAllowNotification.text = getString(R.string.Allow)
                    txtAllowNotification.setTextColor(requireContext().getColor(R.color.permissionAllowText))
                }
            }
            else{
                txtAllowNotification.text = getString(R.string.Done)
                txtAllowNotification.setTextColor(requireContext().getColor(R.color.permissionDoneText))
            }
            if (audioSettingPermission) {
                txtAllowAudio.text = getString(R.string.Done)
                txtAllowAudio.setTextColor(requireContext().getColor(R.color.permissionDoneText))
            } else {
                txtAllowAudio.text = getString(R.string.Allow)
                txtAllowAudio.setTextColor(requireContext().getColor(R.color.permissionAllowText))
            }
            return audioSettingPermission && notificationPermission && doNotDisturb
        }

    }
    val settingsActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        binding.btnStart.isEnabled=permissionCheck()
    }

}