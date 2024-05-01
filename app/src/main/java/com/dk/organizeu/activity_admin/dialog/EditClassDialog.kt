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
import com.dk.organizeu.databinding.EditClassDialogLayoutBinding
import com.dk.organizeu.listener.ClassDocumentListener
import com.dk.organizeu.pojo.ClassPojo
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore

class EditClassDialog(val academicDocumentId: String, val semesterDocumentId: String, val oldClassPojo: ClassPojo) : AppCompatDialogFragment() {
    private lateinit var db: FirebaseFirestore
    private var classDocumentListener: ClassDocumentListener? = null

    private lateinit var binding: EditClassDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-EditClassDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Obtain LayoutInflater from the fragment's context
        val inflater = LayoutInflater.from(requireContext())
        // Inflate the layout for adding a new class
        val view = inflater.inflate(R.layout.edit_class_dialog_layout, null)
        // Bind the layout to the view binding class
        binding = DataBindingUtil.bind(view)!!
        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance()
        var builder: AlertDialog.Builder?=null
        try {
            // Set the classAddListener if the parentFragment implements EditDocumentListener
            classDocumentListener = parentFragment as? ClassDocumentListener
            var title = "Edit Class"
            binding.etClassName.setText(oldClassPojo.name)
            // Create an AlertDialog.Builder instance with the provided context, view, and title
            builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle(title)


            binding.apply {
                btnClose.setOnClickListener {
                    // dismiss the dialog when the "Close" button is clicked
                    dismiss()
                }
                btnEdit.setOnClickListener {
                    try {
                        // get class name from input field
                        val classDocumentId = etClassName.text.toString().trim().replace(Regex("\\s+")," ")

                        if(!classDocumentId.containsOnlyAllowedCharacters())
                        {
                            tlClassName.error = "Class name only allows alphabets, -, _."
                            return@setOnClickListener
                        }



                        tlClassName.error = null


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


}