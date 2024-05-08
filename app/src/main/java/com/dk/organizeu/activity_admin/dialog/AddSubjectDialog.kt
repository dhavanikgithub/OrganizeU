package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddSubjectDialogLayoutBinding
import com.dk.organizeu.enum_class.SubjectType
import com.dk.organizeu.firebase.key_mapping.SubjectCollection
import com.dk.organizeu.listener.SubjectDocumentListener
import com.dk.organizeu.pojo.SubjectPojo
import com.dk.organizeu.pojo.SubjectPojo.Companion.toSubjectPojo
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.isValidSubjectCode
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddSubjectDialog(val subjectPojo: SubjectPojo?, val position: Int) : AppCompatDialogFragment() {
    private lateinit var db: FirebaseFirestore
    private var subjectDocumentListener: SubjectDocumentListener? = null

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
        binding = DataBindingUtil.bind(view)!!
        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance()
        var builder: AlertDialog.Builder?=null
        try {
            // Set the subjectAddListener if the parentFragment implements AddDocumentListener
            subjectDocumentListener = parentFragment as? SubjectDocumentListener
            var title = "Add Subject"
            if(subjectPojo!=null)
            {
                binding.etSubjectName.setText(subjectPojo.name)
                binding.etSubjectCode.setText(subjectPojo.code)
                if(subjectPojo.type == SubjectType.PRACTICAL.name)
                {
                    binding.chipPractical.isChecked = true
                }
                else if(subjectPojo.type == SubjectType.THEORY.name)
                {
                    binding.chipTheory.isChecked = true
                }
                else{
                    binding.chipPractical.isChecked = true
                    binding.chipTheory.isChecked = true
                }
                binding.btnAdd.text = "Edit"
                binding.btnAdd.setIconResource(R.drawable.ic_edit)
                title = "Edit Subject"
            }
            // Create an AlertDialog.Builder instance with the provided context, view, and title
            builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle(title)

            binding.apply {
                btnClose.setOnClickListener {
                    // dismiss the dialog when the "Close" button is clicked
                    dismiss()
                }
                btnAdd.setOnClickListener {
                    try {
                        // get subject name from input field
                        val subjectDocumentId = etSubjectName.text.toString().trim().replace(Regex("\\s+")," ")
                        val subjectCode = etSubjectCode.text.toString().trim()
                        // Check if subject name, subject code, and at least one type (practical or theory) is selected
                        if(subjectDocumentId!="" && subjectCode!="" && (chipPractical.isChecked || chipTheory.isChecked))
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

                            // hashmap dataset of subject
                            val subjectData = hashMapOf(
                                SubjectCollection.CODE.displayName to subjectCode,
                                SubjectCollection.TYPE.displayName to subjectType
                            )
                            if(!subjectDocumentId.containsOnlyAllowedCharacters())
                            {
                                tlSubjectName.error = "Subject name only contain alphabets, number and - or  _ "
                                return@setOnClickListener
                            }
                            tlSubjectName.error = null
                            if(!subjectCode.isValidSubjectCode())
                            {
                                tlSubjectCode.error = "Subject code only contain alphabets, number and - or  _  with length 5 to 15"
                                return@setOnClickListener
                            }
                            tlSubjectCode.error = null

                            if(subjectPojo!=null)
                            {
                                // Check if the subject document already exists
                                SubjectRepository.isSubjectDocumentExistsById(subjectPojo.id) { exists ->
                                    try {
                                        if(!exists)
                                        {
                                            requireContext().showToast("Subject is not exists")
                                            return@isSubjectDocumentExistsById
                                        }
                                        subjectPojo.name = subjectDocumentId
                                        subjectPojo.code = subjectCode
                                        subjectPojo.type = subjectType

                                        MainScope().launch(Dispatchers.IO)
                                        {
                                            var flag = true
                                            val subjectDocuments = SubjectRepository.getAllSubjectDocuments()
                                            for(item in subjectDocuments)
                                            {
                                                val temp = item.toSubjectPojo()
                                                if(temp.id != subjectPojo.id)
                                                {
                                                    if(subjectPojo.name == temp.name || subjectPojo.code == temp.code)
                                                    {
                                                        flag = false
                                                        break
                                                    }
                                                }
                                            }
                                            withContext(Dispatchers.Main)
                                            {
                                                if(!flag)
                                                {
                                                    requireContext().showToast("Subject name or code can't duplicate")
                                                    return@withContext
                                                }
                                                SubjectRepository.updateSubjectDocument(subjectPojo){isUpdated ->
                                                    if(isUpdated)
                                                    {
                                                        subjectDocumentListener!!.onEdited(subjectPojo,position)
                                                        dismiss()
                                                    }
                                                }
                                            }
                                        }

                                    } catch (e: Exception) {
                                        // Log any unexpected exceptions that occur
                                        Log.e(TAG,e.message.toString())
                                        // Display an unexpected error message to the user
                                        requireContext().unexpectedErrorMessagePrint(e)
                                        throw e
                                    }
                                }
                                return@setOnClickListener
                            }
                            val newSubjectPojo = SubjectPojo(
                                name = subjectDocumentId,
                                code = subjectCode,
                                type = subjectType
                            )
                            // Check if the subject document already exists
                            SubjectRepository.isSubjectDocumentExistsByNameAndCode(newSubjectPojo) { exists ->
                                try {
                                    if(exists)
                                    {
                                        requireContext().showToast("Subject is exists")
                                        return@isSubjectDocumentExistsByNameAndCode
                                    }

                                    // Add new subject if the subject document does not exist
                                    addNewSubject(newSubjectPojo)
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
    private fun addNewSubject(subjectPojo: SubjectPojo)
    {
        SubjectRepository.insertSubjectDocument(subjectPojo,{
            // Success Callback
            try {
                Log.d("TAG", "Subject document added successfully with ID: ${subjectPojo.id}")
                // Notify the listener about the addition of the subject document
                subjectDocumentListener?.onAdded(subjectPojo)
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