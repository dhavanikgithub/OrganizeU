package com.dk.organizeu.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import com.dk.organizeu.R
import com.google.android.material.button.MaterialButton

class UnderConstructionDialog(context: Context) {
    val dialog = Dialog(context)

    fun show(positiveButtonClick:(View)->Unit)
    {
        val btnGoBack = dialog.findViewById<MaterialButton>(R.id.btnGoBack)
        btnGoBack.setOnClickListener(positiveButtonClick)
        dialog.show()
    }

    fun dismiss()
    {
        dialog.dismiss()
    }

    fun build():UnderConstructionDialog
    {
        dialog.setContentView(R.layout.work_in_progress_dialog_layout)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return this
    }

    fun setCancelable(flag:Boolean): UnderConstructionDialog
    {
        dialog.setCancelable(flag)
        return this
    }
}