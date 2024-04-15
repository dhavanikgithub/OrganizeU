package com.dk.organizeu.admin_activity.dialog

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
import com.dk.organizeu.enum_class.SubjectType
import com.dk.organizeu.firebase.key_mapping.SubjectCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.repository.SubjectRepository.Companion.isSubjectDocumentExists
import com.dk.organizeu.utils.UtilFunction.Companion.hideKeyboard
import com.dk.organizeu.utils.UtilFunction.Companion.isItemSelected
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
    private var subjectAddListener: AddDocumentListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_subject_dialog_layout, null)
        db = FirebaseFirestore.getInstance()
        subjectAddListener = parentFragment as? AddDocumentListener
        subjectTypeACTV = view.findViewById(R.id.actSubjectType)
        subjectCodeET = view.findViewById(R.id.etSubjectCode)
        subjectNameET = view.findViewById(R.id.etSubjectName)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)

        val subjectTypeList = arrayOf(
            SubjectType.THEORY.name,
            SubjectType.PRACTICAL.name,
            SubjectType.BOTH.name)
        val subjectTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjectTypeList)
        subjectTypeACTV.setAdapter(subjectTypeAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Subject")


        subjectTypeACTV.setOnClickListener {
            requireContext().hideKeyboard(it)
        }

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {
            if(isItemSelected(subjectTypeACTV) && subjectNameET.text.toString()!="" && subjectCodeET.text.toString()!="")
            {
                val subjectDocumentId = subjectNameET.text.toString()
                val subjectData = hashMapOf(
                    SubjectCollection.CODE.displayName to subjectCodeET.text.toString(),
                    SubjectCollection.TYPE.displayName to subjectTypeACTV.text.toString()
                )
                isSubjectDocumentExists(subjectDocumentId) { exists ->
                    if(exists)
                    {
                        closeButton.callOnClick()
                        return@isSubjectDocumentExists
                    }
                    addNewSubject(subjectDocumentId,subjectData)
                }

            }

        }
        return builder.create()
    }


    private fun addNewSubject(subjectDocumentId:String, subjectData:HashMap<String,String>)
    {
        SubjectRepository.insertSubjectDocument(subjectDocumentId,subjectData,{
            Log.d("TAG", "Subject document added successfully with ID: $subjectDocumentId")
            subjectAddListener?.onAdded(subjectDocumentId,subjectData)
            closeButton.callOnClick()
        },{
            Log.w("TAG", "Error adding subject document", it)
            closeButton.callOnClick()
        })
    }
}