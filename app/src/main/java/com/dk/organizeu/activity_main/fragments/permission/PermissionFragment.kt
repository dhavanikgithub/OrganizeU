package com.dk.organizeu.activity_main.fragments.permission

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.activity_student.StudentActivity
import com.dk.organizeu.databinding.FragmentPermissionBinding
import com.dk.organizeu.enum_class.AdminLocalDBKey
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.utils.Constants.Companion.AUDIO_SETTING_PERMISSION
import com.dk.organizeu.utils.Constants.Companion.NOTIFICATION_PERMISSION
import com.dk.organizeu.utils.PermissionManager.Companion.appNotificationSettingIntent
import com.dk.organizeu.utils.PermissionManager.Companion.appSettingIntent
import com.dk.organizeu.utils.PermissionManager.Companion.isAudioSettingsPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.isDoNotDisturbPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.isNotificationPermissionGranted
import com.dk.organizeu.utils.PermissionManager.Companion.requestPermission
import com.dk.organizeu.utils.SharedPreferencesManager
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
                btnStart.isEnabled = permissionCheck()
                // Set up click listener for the start button to navigate to the splash fragment
                btnStart.setOnClickListener {
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

                // Set up click listener for the allow notification button
                btnAllowNotification.setOnClickListener {
                    btnNotificationClickListener()
                }

                // Set up click listener for the allow do not disturb button
                btnAllowDoNotDisturb.setOnClickListener {
                    try {
                        // Check if the permission to access do not disturb mode is not granted
                        if (txtAllowDoNotDisturb.text.toString().equals(getString(R.string.Allow), true)) {
                            if (!isDoNotDisturbPermissionGranted(requireContext())) {
                                // If not granted, launch the notification policy access intent
                                appNotificationSettingIntent().apply {
                                    notificationPolicyAccessLauncher.launch(this)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Handle any exceptions that occur during do not disturb permission check
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }

                // Set up click listener for the allow audio button
                btnAllowAudio.setOnClickListener {
                    btnAudioClickListener()
                }
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

    fun isStudentAlreadyLogin():Boolean{
        return SharedPreferencesManager.containsKey(requireContext(), StudentLocalDBKey.ID.displayName)
    }

    fun isAdminAlreadyLogin():Boolean{
        return SharedPreferencesManager.containsKey(requireContext(), AdminLocalDBKey.ID.displayName)
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


    /**
     * Handles the click event for the allow notification button.
     * This function checks if the notification permission is allowed,
     * requests the permission if not, and launches the app settings if needed.
     */
    fun btnNotificationClickListener() {
        binding.apply {
            // Check if the button text indicates permission is allowed
            if (txtAllowNotification.text.toString().equals(getString(R.string.Allow), true)) {
                // Check if the device's SDK version is supported
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Request notification permission using a custom function
                    requestPermission(requireContext(), NOTIFICATION_PERMISSION,
                        {
                            // Permission granted callback
                            try {
                                // Update UI based on permission status
                                btnStart.isEnabled = permissionCheck()
                            } catch (e: Exception) {
                                // Handle any exceptions that occur
                                requireContext().unexpectedErrorMessagePrint(e)
                            }
                        },
                        {
                            // Permission denied callback
                            try {
                                // Launch app settings to allow the user to grant permission manually
                                appSettingIntent(requireContext()).apply {
                                    settingsActivityResultLauncher.launch(this)
                                }
                            } catch (e: Exception) {
                                // Handle any exceptions that occur
                                requireContext().unexpectedErrorMessagePrint(e)
                            }
                        })
                } else {
                    // If the SDK version is not supported, update UI based on permission status
                    btnStart.isEnabled = permissionCheck()
                }
            }
        }
    }

    /**
     * Handles the click event for the allow audio button.
     * This function checks if the audio setting permission is allowed,
     * requests the permission if not, and launches the app settings if needed.
     */
    fun btnAudioClickListener() {
        binding.apply {
            // Check if the button text indicates permission is allowed
            if (txtAllowAudio.text.toString().equals(getString(R.string.Allow), true)) {
                // Request audio setting permission using a custom function
                requestPermission(requireContext(), AUDIO_SETTING_PERMISSION,
                    {
                        // Permission granted callback
                        try {
                            // Update UI based on permission status
                            btnStart.isEnabled = permissionCheck()
                        } catch (e: Exception) {
                            // Handle any exceptions that occur
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    },
                    {
                        // Permission denied callback
                        try {
                            // Launch app settings to allow the user to grant permission manually
                            appSettingIntent(requireContext()).apply {
                                settingsActivityResultLauncher.launch(this)
                            }
                        } catch (e: Exception) {
                            // Handle any exceptions that occur
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    })
            }
        }
    }



    /**
     * Activity result launcher for handling notification policy access request.
     * This launcher is registered to handle the result of starting an activity for
     * granting notification policy access. It enables the start button if the permission is granted.
     */
    private val notificationPolicyAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            // Enable/Disable Start button based on permission status after the activity result is received
            binding.btnStart.isEnabled = permissionCheck()
        } catch (e: Exception) {
            // Handle any exceptions that occur
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }


    /**
     * Activity result launcher for handling app settings request.
     * This launcher is registered to handle the result of starting an activity for
     * opening the app settings. It enables the start button if the permission is granted.
     */
    private val settingsActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            // Enable/Disable Start button based on permission status after the activity result is received
            binding.btnStart.isEnabled = permissionCheck()
        } catch (e: Exception) {
            // Log and rethrow any exceptions that occur
            Log.e(TAG, e.message.toString())
            throw e
        }
    }


    /**
     * Sets the visual indicators to indicate that the permission is done.
     * This function updates the text color and background color of the given TextView and LinearLayout
     * to visually indicate that the permission has been granted or completed.
     *
     * @param textView The TextView to update the text and text color.
     * @param layout The LinearLayout to update the background color.
     */
    private fun setPermissionDone(textView: TextView, layout: LinearLayout) {
        // Update text and text color of the TextView
        textView.text = ""
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
        DrawableCompat.setTint(drawable!!, ContextCompat.getColor(requireContext(), R.color.colorSecondary))
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null,null)
        // Update background color of the LinearLayout
        layout.setBackgroundColor(requireContext().getColor(R.color.colorSecondaryContainer))
    }


    /**
     * Sets the visual indicators to indicate that permission is allowed.
     * This function updates the text color and background color of the given TextView and LinearLayout
     * to visually indicate that permission is allowed or pending.
     *
     * @param textView The TextView to update the text and text color.
     * @param layout The LinearLayout to update the background color.
     */
    private fun setPermissionAllow(textView: TextView, layout: LinearLayout) {
        // Update text and text color of the TextView
        textView.text = getString(R.string.Allow)
        textView.setTextColor(requireContext().getColor(R.color.colorPrimary))
        // Update background color of the LinearLayout
        layout.setBackgroundColor(requireContext().getColor(R.color.colorPrimaryContainer))
    }


    /**
     * Checks the status of various permissions required by the app.
     * This function checks if audio settings permission, notification permission, and do not disturb permission are granted.
     * It updates the UI components to indicate the status of each permission.
     *
     * @return true if all permissions are granted, false otherwise.
     */
    private fun permissionCheck(): Boolean {
        binding.apply {
            try {
                var notificationPermission = true // Assume notification permission is granted by default
                val audioSettingPermission = isAudioSettingsPermissionGranted(requireContext())
                val doNotDisturb = isDoNotDisturbPermissionGranted(requireContext())

                // Do not disturb
                if (doNotDisturb) {
                    setPermissionDone(binding.txtAllowDoNotDisturb, binding.btnAllowDoNotDisturb)
                } else {
                    setPermissionAllow(binding.txtAllowDoNotDisturb, binding.btnAllowDoNotDisturb)
                }

                // Notification Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission = isNotificationPermissionGranted(requireContext())
                    if (notificationPermission) {
                        setPermissionDone(txtAllowNotification, btnAllowNotification)
                    } else {
                        setPermissionAllow(txtAllowNotification, btnAllowNotification)
                    }
                } else {
                    setPermissionDone(txtAllowNotification, btnAllowNotification)
                }

                // Audio Permission
                if (audioSettingPermission) {
                    setPermissionDone(txtAllowAudio, btnAllowAudio)
                } else {
                    setPermissionAllow(txtAllowAudio, btnAllowAudio)
                }

                // Return true if all permissions are granted, false otherwise
                return audioSettingPermission && notificationPermission && doNotDisturb
            } catch (e: Exception) {
                // Rethrow any exceptions that occur
                throw e
            }
        }
    }

}