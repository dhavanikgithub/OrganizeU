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
    private lateinit var academicYearACTV: AutoCompleteTextView
    private lateinit var academicTypeTIL: AutoCompleteTextView
    private lateinit var addButton: MaterialButton
    private lateinit var closeButton: MaterialButton
    private var academicAddListener: AddDocumentListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_academic_dialog_layout, null)

        academicAddListener = parentFragment as? AddDocumentListener
        academicYearACTV = view.findViewById(R.id.actAcademicYear)
        academicTypeTIL = view.findViewById(R.id.actAcademicType)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val academicYears = mutableListOf<String>()
        for (year in currentYear..currentYear + 5) {
            academicYears.add("$year-${year + 1}")
        }

        val academicYearsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYears)
        academicYearACTV.setAdapter(academicYearsAdapter)

        val academicTypes = arrayOf(AcademicType.EVEN.name, AcademicType.ODD.name)
        val academicTypesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypes)
        academicTypeTIL.setAdapter(academicTypesAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Academic")

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {
            if(isItemSelected(academicYearACTV))
            {
                if(isItemSelected(academicTypeTIL))
                {
                    val academicDocumentId = "${academicYearACTV.text}_${academicTypeTIL.text}"
                    val academicData = hashMapOf(
                        AcademicCollection.YEAR.displayName to academicYearACTV.text.toString(),
                        AcademicCollection.TYPE.displayName to academicTypeTIL.text.toString()
                    )

                    isAcademicDocumentExists(academicDocumentId) { exists ->
                        if(exists)
                        {
                            closeButton.callOnClick()
                            return@isAcademicDocumentExists
                        }
                        MainScope().launch(Dispatchers.IO){
                            AcademicRepository.insertAcademicDocuments(academicDocumentId,academicData,{
                                academicAddListener?.onAdded(academicDocumentId,academicData)
                                closeButton.callOnClick()
                            },{

                            })
                        }
                    }

                }
            }

        }
        return builder.create()
    }
}