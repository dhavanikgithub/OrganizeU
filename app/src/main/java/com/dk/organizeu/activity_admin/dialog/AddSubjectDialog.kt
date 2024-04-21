package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddSubjectDialogLayoutBinding
import com.dk.organizeu.firebase.key_mapping.SubjectCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.repository.SubjectRepository.Companion.isSubjectDocumentExists
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.HashMap

class AddSubjectDialog() : AppCompatDialogFragment() {
    private lateinit var db: FirebaseFirestore
    private var subjectAddListener: AddDocumentListener? = null

    private lateinit var binding: AddSubjectDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-AddSubjectDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_subject_dialog_layout, null)
        binding = AddSubjectDialogLayoutBinding.bind(view)
        db = FirebaseFirestore.getInstance()
        var builder: AlertDialog.Builder?=null
        try {
            subjectAddListener = parentFragment as? AddDocumentListener
            builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Add Subject")
            binding.apply {
                btnClose.setOnClickListener {
                    dismiss()
                }
                btnAdd.setOnClickListener {
                    try {
                        if(etSubjectName.text.toString()!="" && etSubjectCode.text.toString()!="" && (chipPractical.isChecked || chipTheory.isChecked))
                        {
                            val subjectType = if(chipPractical.isChecked && chipTheory.isChecked) {
                                "${chipTheory.text} AND ${chipPractical.text}"
                            } else if(chipPractical.isChecked){
                                chipPractical.text.toString()
                            } else{
                                chipTheory.text.toString()
                            }

                            val subjectDocumentId = etSubjectName.text.toString()
                            val subjectData = hashMapOf(
                                SubjectCollection.CODE.displayName to etSubjectCode.text.toString(),
                                SubjectCollection.TYPE.displayName to subjectType
                            )
                            isSubjectDocumentExists(subjectDocumentId) { exists ->
                                try {
                                    if(exists)
                                    {
                                        dismiss()
                                        return@isSubjectDocumentExists
                                    }
                                    addNewSubject(subjectDocumentId,subjectData)
                                } catch (e: Exception) {
                                    Log.e(TAG,e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }

                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }

                }
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
        }

        try {
            return builder!!.create()
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }


    private fun addNewSubject(subjectDocumentId:String, subjectData:HashMap<String,String>)
    {
        SubjectRepository.insertSubjectDocument(subjectDocumentId,subjectData,{
            try {
                Log.d("TAG", "Subject document added successfully with ID: $subjectDocumentId")
                subjectAddListener?.onAdded(subjectDocumentId,subjectData)
                dismiss()
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                requireContext().unexpectedErrorMessagePrint(e)
            }
        },{
            Log.w("TAG", "Error adding subject document", it)
            requireContext().unexpectedErrorMessagePrint(it)
            dismiss()
        })
    }
}