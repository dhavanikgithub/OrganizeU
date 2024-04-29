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
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.isValidSubjectCode
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore

class AddSubjectDialog() : AppCompatDialogFragment() {
    private lateinit var db: FirebaseFirestore
    private var subjectAddListener: AddDocumentListener? = null

    private lateinit var binding: AddSubjectDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-AddSubjectDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Obtain LayoutInflater from the fragment's context
        val inflater = LayoutInflater.from(requireContext())
        // Inflate the layout for adding a new subject
        val view = inflater.inflate(R.layout.add_subject_dialog_layout, null)
        // Bind the layout to the view binding class
        binding = AddSubjectDialogLayoutBinding.bind(view)
        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance()
        var builder: AlertDialog.Builder?=null
        try {
            // Set the subjectAddListener if the parentFragment implements AddDocumentListener
            subjectAddListener = parentFragment as? AddDocumentListener

            // Create an AlertDialog.Builder instance with the provided context, view, and title
            builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Add Subject")

            binding.apply {
                btnClose.setOnClickListener {
                    // dismiss the dialog when the "Close" button is clicked
                    dismiss()
                }
                btnAdd.setOnClickListener {
                    try {
                        // Check if subject name, subject code, and at least one type (practical or theory) is selected
                        if(etSubjectName.text.toString().trim()!="" && etSubjectCode.text.toString().trim()!="" && (chipPractical.isChecked || chipTheory.isChecked))
                        {
                            // Determine the subject type based on chip selection whether
                            // subject type is practical only , theory only or both practical and theory
                            val subjectType = if(chipPractical.isChecked && chipTheory.isChecked) {
                                "${chipTheory.text} AND ${chipPractical.text}"
                            } else if(chipPractical.isChecked){
                                chipPractical.text.toString()
                            } else{
                                chipTheory.text.toString()
                            }

                            // get subject name from input field
                            val subjectDocumentId = etSubjectName.text.toString().trim()
                            val subjectCode = etSubjectCode.text.toString().trim()

                            // hashmap dataset of subject
                            val subjectData = hashMapOf(
                                SubjectCollection.CODE.displayName to subjectCode,
                                SubjectCollection.TYPE.displayName to subjectType
                            )

                            // Check if the subject document already exists
                            isSubjectDocumentExists(subjectDocumentId, subjectCode) { exists ->
                                try {
                                    if(exists)
                                    {
                                        requireContext().showToast("Subject is exists")
                                        return@isSubjectDocumentExists
                                    }
                                    if(!subjectDocumentId.containsOnlyAllowedCharacters())
                                    {
                                        tlSubjectName.error = "Subject name only contain alphabets, number and - or  _ "
                                        return@isSubjectDocumentExists
                                    }
                                    tlSubjectName.error = null
                                    if(!subjectCode.isValidSubjectCode())
                                    {
                                        tlSubjectCode.error = "Subject code only contain alphabets, number and - or  _  with length 5 to 15"
                                        return@isSubjectDocumentExists
                                    }
                                    tlSubjectCode.error = null
                                    // Add new subject if the subject document does not exist
                                    addNewSubject(subjectDocumentId,subjectData)
                                } catch (e: Exception) {
                                    // Log any unexpected exceptions that occur
                                    Log.e(TAG,e.message.toString())
                                    // Display an unexpected error message to the user
                                    requireContext().unexpectedErrorMessagePrint(e)
                                    throw e
                                }
                            }

                        }
                    } catch (e: Exception) {
                        // Log any unexpected exceptions that occur
                        Log.e(TAG,e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }

                }
            }
        } catch (e: Exception) {
            // Log any unexpected exceptions that occur
            Log.e(TAG,e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
        }

        try {
            return builder!!.create()
        } catch (e: Exception) {
            // Log any unexpected exceptions that occur
            Log.e(TAG,e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }

    /**
     * Adds a new subject document to the database.
     *
     * @param subjectDocumentId The ID of the subject document to be inserted.
     * @param subjectData The data of the subject document to be inserted.
     */
    private fun addNewSubject(subjectDocumentId:String, subjectData:HashMap<String,String>)
    {
        SubjectRepository.insertSubjectDocument(subjectDocumentId,subjectData,{
            // Success Callback
            try {
                Log.d("TAG", "Subject document added successfully with ID: $subjectDocumentId")
                // Notify the listener about the addition of the subject document
                subjectAddListener?.onAdded(subjectDocumentId,subjectData)
                // Dismiss the dialog after adding the subject document
                dismiss()
            } catch (e: Exception) {
                // Log any unexpected exceptions that occur
                Log.e(TAG,e.message.toString())
                // Display an unexpected error message to the user
                requireContext().unexpectedErrorMessagePrint(e)
            }
        },{
            // Error Callback
            Log.w("TAG", "Error adding subject document", it)
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(it)
            dismiss() // Dismiss the dialog
        })
    }
}