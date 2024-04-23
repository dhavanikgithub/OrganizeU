package com.dk.organizeu.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class PermissionManager {
    companion object{
        const val TAG = "OrganizeU-PermissionManager"

        fun isPermissionGranted(context: Context, PERMISSION: String):Boolean{
            return ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        fun isDoNotDisturbPermissionGranted(context: Context):Boolean{
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                return notificationManager.isNotificationPolicyAccessGranted
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isAudioSettingsPermissionGranted(context: Context): Boolean
        {
            try {
                return isPermissionGranted(context, Constants.AUDIO_SETTING_PERMISSION)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun isNotificationPermissionGranted(context: Context): Boolean
        {
            try {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    isPermissionGranted(context, Constants.NOTIFICATION_PERMISSION)
                }
                else{
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun appSettingIntent(context: Context): Intent
        {
            try {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        return this
                    }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                throw e
            }
        }

        fun appNotificationSettingIntent(): Intent{
            return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        }

        fun requestPermission(
            context: Context,
            permission: String,
            permissionGranted:(permissionGrantedResponse : PermissionGrantedResponse?) -> Unit,
            permissionDenied:(permissionDeniedResponse : PermissionDeniedResponse?)->Unit
        )
        {
            Dexter.withContext(context).withPermission(permission)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        permissionGranted(p0)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        permissionDenied(p0)
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