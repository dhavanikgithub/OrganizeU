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
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class AddAcademicDialog() : AppCompatDialogFragment() {
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var autoCompleteTextView2: AutoCompleteTextView
    private lateinit var addButton: MaterialButton
    private lateinit var closeButton: MaterialButton
    private lateinit var db: FirebaseFirestore
    private var academicAddListener: AcademicAddListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_academic_dialog_layout, null)
        db = FirebaseFirestore.getInstance()
        academicAddListener = parentFragment as? AcademicAddListener
        autoCompleteTextView = view.findViewById(R.id.academicYearACTV)
        autoCompleteTextView2 = view.findViewById(R.id.academicTypeACTV)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)

        // Get the current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Generate academic years up to five years after the current year
        val academicYears = mutableListOf<String>()
        for (year in currentYear..currentYear + 5) {
            academicYears.add("$year-${year + 1}")
        }

        // Set up the adapter for academic years dropdown
        val academicYearsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYears)
        autoCompleteTextView.setAdapter(academicYearsAdapter)

        // Set up the adapter for academic types dropdown
        val academicTypes = arrayOf(AcademicType.EVEN.name, AcademicType.ODD.name)
        val academicTypesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicTypes)
        autoCompleteTextView2.setAdapter(academicTypesAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Academic")

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {
            if(isItemSelected(autoCompleteTextView))
            {
                if(isItemSelected(autoCompleteTextView2))
                {
                    val academicDocumentId = "${autoCompleteTextView.text}_${autoCompleteTextView2.text}"
                    val academicData = hashMapOf(
                        "year" to autoCompleteTextView.text.toString(),
                        "type" to autoCompleteTextView2.text.toString()

                    )
                    isAcademicDocumentExists(academicDocumentId) { exists ->
                        if(exists)
                        {
                            closeButton.callOnClick()
                            return@isAcademicDocumentExists
                        }
                        addNewAcademic(academicDocumentId,academicData)
                    }

                }
            }

        }
        return builder.create()
    }

    private fun addNewAcademic(academicDocumentId:String,academicData:HashMap<String,String>)
    {
        db.collection("academic")
            .document(academicDocumentId)
            .set(academicData)
            .addOnSuccessListener {
                Log.d("TAG", "Academic document added successfully with ID: $academicDocumentId")
                academicAddListener?.onAcademicAdded(academicDocumentId)
                closeButton.callOnClick()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding academic document", e)
                closeButton.callOnClick()
            }
    }

    private fun isAcademicDocumentExists(academicDocumentId: String, callback: (Boolean) -> Unit) {
        db.collection("academic")
            .document(academicDocumentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                callback(documentSnapshot.exists())
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error checking document existence", exception)
                callback(false) // Assume document doesn't exist if there's an error
            }
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