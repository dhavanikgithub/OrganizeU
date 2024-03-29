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
import com.dk.organizeu.admin_activity.enum_class.SubjectType
import com.dk.organizeu.admin_activity.listener.SubjectAddListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.HashMap

class AddSubjectDialog() : AppCompatDialogFragment() {
    private lateinit var subjectTypeACTV: AutoCompleteTextView
    private lateinit var subjectNameET: TextInputEditText
    private lateinit var subjectCodeET: TextInputEditText
    private lateinit var addButton: MaterialButton
    private lateinit var closeButton: MaterialButton

    private lateinit var db: FirebaseFirestore
    private var subjectAddListener: SubjectAddListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_subject_dialog_layout, null)
        db = FirebaseFirestore.getInstance()
        subjectAddListener = parentFragment as? SubjectAddListener
        subjectTypeACTV = view.findViewById(R.id.subjectTypeACTV)
        subjectCodeET = view.findViewById(R.id.subjectCodeET)
        subjectNameET = view.findViewById(R.id.subjectNameET)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)

        val subjectTypeList = arrayOf(SubjectType.THEORY.name,SubjectType.PRACTICAL.name,SubjectType.BOTH.name)
        val subjectTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjectTypeList)
        subjectTypeACTV.setAdapter(subjectTypeAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Subject")

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {
            if(isItemSelected(subjectTypeACTV) && subjectNameET.text.toString()!="" && subjectCodeET.text.toString()!="")
            {

                val roomData = hashMapOf(
                    "code" to subjectCodeET.text.toString(),
                    "type" to subjectTypeACTV.text.toString()
                )
                isSubjectDocumentExists(subjectNameET.text.toString()) { exists ->
                    if(exists)
                    {
                        closeButton.callOnClick()
                        return@isSubjectDocumentExists
                    }
                    addNewSubject(subjectNameET.text.toString(),roomData)
                }

            }

        }
        return builder.create()
    }

    private fun addNewSubject(subjectDocumentId:String, subjectData:HashMap<String,String>)
    {
        db.collection("subject")
            .document(subjectDocumentId)
            .set(subjectData)
            .addOnSuccessListener {
                Log.d("TAG", "Subject document added successfully with ID: $subjectDocumentId")
                subjectAddListener?.onSubjectAdded(subjectData,subjectDocumentId)
                closeButton.callOnClick()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding subject document", e)
                closeButton.callOnClick()
            }
    }

    private fun isSubjectDocumentExists(roomDocumentId: String, callback: (Boolean) -> Unit) {
        db.collection("subject")
            .document(roomDocumentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                callback(documentSnapshot.exists())
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error checking document existence", exception)
                callback(false)
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