package com.dk.organizeu.admin_activity.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddAcademicDialogLayoutBinding
import com.dk.organizeu.enum_class.AcademicType
import com.dk.organizeu.firebase.key_mapping.AcademicCollection
import com.dk.organizeu.listener.AddDocumentListener

import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.utils.UtilFunction.Companion.isItemSelected
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class AddAcademicDialog() : AppCompatDialogFragment() {
    private var academicAddListener: AddDocumentListener? = null
    private lateinit var binding: AddAcademicDialogLayoutBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_academic_dialog_layout, null)
        binding = AddAcademicDialogLayoutBinding.bind(view)
        academicAddListener = parentFragment as? AddDocumentListener
        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Academic")
        binding.apply {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            val academicYears = mutableListOf<String>()
            for (year in currentYear..currentYear + 5) {
                academicYears.add("$year-${year + 1}")
            }

            val academicYearsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYears)
            actAcademicYear.setAdapter(academicYearsAdapter)



            btnClose.setOnClickListener {
                dismiss()
            }
            btnAdd.setOnClickListener {
                if(isItemSelected(actAcademicYear))
                {
                    if((chipEven.isChecked || chipOdd.isChecked))
                    {
                        val aType = if(chipEven.isChecked) chipEven.text.toString() else chipOdd .text.toString()
                        val academicDocumentId = "${actAcademicYear.text}_${aType}"
                        val academicData = hashMapOf(
                            AcademicCollection.YEAR.displayName to actAcademicYear.text.toString(),
                            AcademicCollection.TYPE.displayName to aType
                        )

                        isAcademicDocumentExists(academicDocumentId) { exists ->
                            if(exists)
                            {
                                dismiss()
                                return@isAcademicDocumentExists
                            }
                            MainScope().launch(Dispatchers.IO){
                                AcademicRepository.insertAcademicDocuments(academicDocumentId,academicData,{
                                    academicAddListener?.onAdded(academicDocumentId,academicData)
                                    dismiss()
                                },{

                                })
                            }
                        }

                    }
                }

            }
        }

        return builder.create()
    }
}