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
import com.dk.organizeu.activity_admin.fragments.faculty.FacultyFragment
import com.dk.organizeu.databinding.AddFacultyDialogLayoutBinding
import com.dk.organizeu.listener.FacultyDocumentListener
import com.dk.organizeu.pojo.FacultyPojo
import com.dk.organizeu.repository.FacultyRepository
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore

class AddFacultyDialog(val facultyPojo: FacultyPojo?) : AppCompatDialogFragment() {
    private lateinit var db: FirebaseFirestore
    private var facultyDocumentListener: FacultyDocumentListener? = null

    private lateinit var binding: AddFacultyDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-AddFacultyDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Obtain LayoutInflater from the fragment's context
        val inflater = LayoutInflater.from(requireContext())
        // Inflate the layout for adding a new faculty
        val view = inflater.inflate(R.layout.add_faculty_dialog_layout, null)
        // Bind the layout to the view binding class
        binding = DataBindingUtil.bind(view)!!
        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance()
        var builder: AlertDialog.Builder?=null
        try {
            // Set the facultyAddListener if the parentFragment implements AddDocumentListener
            facultyDocumentListener = parentFragment as? FacultyDocumentListener
            var title = "Add Faculty"
            if(facultyPojo!=null)
            {
                binding.btnAdd.text = "Edit"
                binding.btnAdd.setIconResource(R.drawable.ic_edit)
                binding.etFacultyName.setText(facultyPojo.name)
                title = "Edit Faculty"
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
                        // get faculty name from input field
                        val facultyName = etFacultyName.text.toString().trim().replace(Regex("\\s+")," ")

                        if(!facultyName.matches("^[\\sa-zA-Z_-]{2,20}$".toRegex()))
                        {
                            tlFacultyName.error = "Faculty name only allows alphabets, -, _, with a length of 2-20 characters"
                            return@setOnClickListener
                        }

                        tlFacultyName.error = null
                        if(facultyPojo!=null){
                            FacultyRepository.isFacultyDocumentExistsById(facultyPojo.id) { exists ->
                                if(!exists)
                                {
                                    requireContext().showToast("Faculty is not exist")
                                    return@isFacultyDocumentExistsById
                                }
                                facultyPojo.name = facultyName
                                FacultyRepository.updateFacultyDocument(facultyPojo) {
                                    if(it)
                                    {
                                        facultyDocumentListener!!.onEdited(facultyPojo)
                                        dismiss()
                                    }
                                }
                            }
                            return@setOnClickListener
                        }
                        val newFacultyPojo = FacultyPojo(name = facultyName)

                        // Check if the faculty document already exists
                        FacultyRepository.isFacultyDocumentExistsByName(newFacultyPojo.name) { exists ->
                            try {
                                if(exists)
                                {
                                    requireContext().showToast("Faculty is exists")
                                    return@isFacultyDocumentExistsByName
                                }
                                // Add new faculty if the faculty document does not exist
                                addNewFaculty(newFacultyPojo)
                            } catch (e: Exception) {
                                // Log any unexpected exceptions that occur
                                Log.e(TAG,e.message.toString())
                                // Display an unexpected error message to the user
                                requireContext().unexpectedErrorMessagePrint(e)
                                throw e
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

    private fun addNewFaculty(facultyPojo: FacultyPojo) {
        FacultyRepository.insertFacultyDocument(facultyPojo, {
            try {
                // If insertion is successful
                facultyDocumentListener!!.onAdded(facultyPojo)
                dismiss()
            } catch (e: Exception) {
                // Log any unexpected exceptions that occur
                Log.e(FacultyFragment.TAG, e.message.toString())
                // Display an unexpected error message to the user
                requireContext().unexpectedErrorMessagePrint(e)
                throw e
            }
        }, {
            // Log any unexpected exceptions that occur
            Log.e(FacultyFragment.TAG, it.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(it)
            throw it
        })
    }


}