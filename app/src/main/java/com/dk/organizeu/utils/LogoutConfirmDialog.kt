package com.dk.organizeu.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import com.dk.organizeu.R
import com.google.android.material.button.MaterialButton

class LogoutConfirmDialog(context: Context) {
    val dialog = Dialog(context)

    fun show(positiveButtonClick:(View)->Unit, negativeButtonClick:(View)->Unit)
    {
        val btnLogout = dialog.findViewById<MaterialButton>(R.id.btnLogout)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        btnLogout.setOnClickListener(positiveButtonClick)
        btnCancel.setOnClickListener(negativeButtonClick)
        dialog.show()
    }

    fun dismiss()
    {
        dialog.dismiss()
    }

    fun build():LogoutConfirmDialog
    {
        dialog.setContentView(R.layout.logout_confirm_dialog_layout)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return this
    }

    fun setCancelable(flag:Boolean): LogoutConfirmDialog
    {
        dialog.setCancelable(flag)
        return this
    }
}