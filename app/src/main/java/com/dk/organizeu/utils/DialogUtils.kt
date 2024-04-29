package com.dk.organizeu.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dk.organizeu.R
import com.google.android.material.button.MaterialButton

class DialogUtils(context: Context) {
    val dialog = Dialog(context)

    fun show(positiveButtonClick:(View)->Unit, negativeButtonClick:(View)->Unit)
    {
        val btnOkay = dialog.findViewById<MaterialButton>(R.id.btnOkay)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        btnOkay.setOnClickListener(positiveButtonClick)
        btnCancel.setOnClickListener(negativeButtonClick)
        dialog.show()
    }

    fun dismiss()
    {
        dialog.dismiss()
    }

    fun setTitle(title:String):DialogUtils
    {
        val dialogTitle = dialog.findViewById<TextView>(R.id.txtTitle)
        dialogTitle.text = title
        return this
    }

    fun setMessage(message:String): DialogUtils{
        val dialogMessage = dialog.findViewById<TextView>(R.id.txtMessage)
        dialogMessage.text = message
        return this
    }

    fun build():DialogUtils
    {
        dialog.setContentView(R.layout.delete_item_dialog_layout)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return this
    }

    fun setCancelable(flag:Boolean): DialogUtils
    {
        dialog.setCancelable(flag)
        return this
    }
}