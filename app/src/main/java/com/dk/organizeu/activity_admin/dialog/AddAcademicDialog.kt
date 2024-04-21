package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddAcademicDialogLayoutBinding
import com.dk.organizeu.firebase.key_mapping.AcademicCollection
import com.dk.organizeu.listener.AddDocumentListener

import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.utils.UtilFunction.Companion.isItemSelected
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class AddAcademicDialog() : AppCompatDialogFragment() {
    private var academicAddListener: AddDocumentListener? = null
    private lateinit var binding: AddAcademicDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-AddAcademicDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_academic_dialog_layout, null)
        binding = AddAcademicDialogLayoutBinding.bind(view)
        var builder:AlertDialog.Builder? = null
        try {
            academicAddListener = parentFragment as? AddDocumentListener
            builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Add Academic")
        } catch (e: Exception) {
            requireContext().unexpectedErrorMessagePrint(e)
        }
        binding.apply {
            try {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                val academicYears = mutableListOf<String>()
                for (year in currentYear..currentYear + 5) {
                    academicYears.add("$year-${year + 1}")
                }

                val academicYearsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYears)
                actAcademicYear.setAdapter(academicYearsAdapter)
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                requireContext().unexpectedErrorMessagePrint(e)
            }

            btnClose.setOnClickListener {
                dismiss()
            }
            btnAdd.setOnClickListener {
                try {
                    if(isItemSelected(actAcademicYear))
                    {
                        if((chipEven.isChecked || chipOdd.isChecked))
                        {
                            var academicDocumentId: String? = null
                            var academicData: HashMap<String,String>? = null
                            try {
                                val aType = if(chipEven.isChecked) chipEven.text.toString() else chipOdd .text.toString()
                                academicDocumentId = "${actAcademicYear.text}_${aType}"
                                academicData = hashMapOf(
                                    AcademicCollection.YEAR.displayName to actAcademicYear.text.toString(),
                                    AcademicCollection.TYPE.displayName to aType
                                )
                            } catch (e: Exception) {
                                Log.e(TAG,e.message.toString())
                                requireContext().unexpectedErrorMessagePrint(e)
                            }

                            if (academicDocumentId != null) {
                                isAcademicDocumentExists(academicDocumentId) { exists ->
                                    try {
                                        if(exists) {
                                            dismiss()
                                            return@isAcademicDocumentExists
                                        }
                                        MainScope().launch(Dispatchers.IO){
                                            try {
                                                if (academicData != null) {
                                                    AcademicRepository.insertAcademicDocuments(academicDocumentId,academicData,{
                                                        academicAddListener?.onAdded(academicDocumentId,academicData)
                                                        dismiss()
                                                    },{
                                                        Log.e(TAG,it.message.toString())
                                                        requireContext().unexpectedErrorMessagePrint(it)
                                                    })
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG,e.message.toString())
                                                requireContext().unexpectedErrorMessagePrint(e)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG,e.message.toString())
                                        requireContext().unexpectedErrorMessagePrint(e)
                                    }
                                }
                            }

                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

            }
        }

        try {
            return builder!!.create()
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }
}