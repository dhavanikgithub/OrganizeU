package com.dk.organizeu.admin_activity.dialog_box

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.enum_class.AcademicType
import com.dk.organizeu.admin_activity.listener.AcademicAddListener
import com.dk.organizeu.model.AcademicPojo
import com.dk.organizeu.model.AcademicPojo.Companion.isAcademicDocumentExists
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class AddAcademicDialog() : AppCompatDialogFragment() {
    private lateinit var academicYearACTV: AutoCompleteTextView
    private lateinit var academicTypeTIL: AutoCompleteTextView
    private lateinit var addButton: MaterialButton
    private lateinit var closeButton: MaterialButton
    private var academicAddListener: AcademicAddListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_academic_dialog_layout, null)

        academicAddListener = parentFragment as? AcademicAddListener
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
                        "year" to academicYearACTV.text.toString(),
                        "type" to academicTypeTIL.text.toString()
                    )

                    isAcademicDocumentExists(academicDocumentId) { exists ->
                        if(exists)
                        {
                            closeButton.callOnClick()
                            return@isAcademicDocumentExists
                        }
                        MainScope().launch(Dispatchers.IO){
                            AcademicPojo.insertAcademicDocuments(academicDocumentId,academicData,{
                                academicAddListener?.onAcademicAdded(academicDocumentId)
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





    private fun isItemSelected(autoCompleteTextView: AutoCompleteTextView): Boolean {
        val selectedItem = autoCompleteTextView.text.toString().trim()
        val adapter = autoCompleteTextView.adapter as ArrayAdapter<String>
        for (i in 0 until adapter.count) {
            if (selectedItem == adapter.getItem(i)) {
                return true
            }
        }
        return false
    }

}