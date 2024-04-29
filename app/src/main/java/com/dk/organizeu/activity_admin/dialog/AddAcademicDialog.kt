package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import com.dk.organizeu.R
import com.dk.organizeu.databinding.AddAcademicDialogLayoutBinding
import com.dk.organizeu.firebase.key_mapping.AcademicCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.repository.AcademicRepository.Companion.isAcademicDocumentExists
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.dk.organizeu.utils.Validation.Companion.isItemSelected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddAcademicDialog() : AppCompatDialogFragment() {
    private var academicAddListener: AddDocumentListener? = null
    private lateinit var binding: AddAcademicDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-AddAcademicDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_academic_dialog_layout, null)
        binding = DataBindingUtil.bind(view)!!
        var builder:AlertDialog.Builder? = null
        academicAddListener = parentFragment as? AddDocumentListener
        // Create object of AlertDialog box
        builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Academic")
        binding.apply {
            try {
                // Prepare academic year list for Initialize academic year drop down
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val academicYears = mutableListOf<String>()
                // Prepare academic year list from now to up to 5 year before
                for (year in currentYear..currentYear + 5) {
                    academicYears.add("$year-${year + 1}")
                }

                // Initialize the Academic year drop down
                val academicYearsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, academicYears)
                actAcademicYear.setAdapter(academicYearsAdapter)
            } catch (e: IllegalStateException) {
                // Handle IllegalStateException
                requireContext().showToast("Could not initialize academic year list. Please try again later.")
                Log.e(TAG, "An IllegalStateException occurred", e)
            } catch (e: IllegalArgumentException) {
                // Handle IllegalArgumentException
                requireContext().showToast("Could not initialize academic year list due to invalid arguments. Please try again later.")
                Log.e(TAG, "An IllegalArgumentException occurred", e)
            } catch (e: Exception) {
                // Handle other exceptions
                requireContext().showToast("An unexpected error occurred. Please try again later.")
                Log.e(TAG, "An unexpected error occurred", e)
            }

            // closed dialog box when user click on close button of dialog box
            btnClose.setOnClickListener {
                dismiss()
            }

            btnAdd.setOnClickListener {
                try {
                    // process further if academic year is selected
                    if(isItemSelected(actAcademicYear))
                    {
                        // execute next process for adding document in database if any one academic type is checked
                        if((chipEven.isChecked || chipOdd.isChecked))
                        {
                            var academicDocumentId: String? = null
                            var academicData: HashMap<String,String>? = null
                            // Determine the academic type based on the checked state of chips
                            // If the chipEven is checked, set the academic type to EVEN
                            // If the chipOdd is checked, set the academic type to ODD
                            val aType = if(chipEven.isChecked) chipEven.text.toString() else chipOdd .text.toString()

                            academicDocumentId = "${actAcademicYear.text}_${aType}"

                            academicData = hashMapOf(
                                AcademicCollection.YEAR.displayName to actAcademicYear.text.toString(),
                                AcademicCollection.TYPE.displayName to aType
                            )

                            if (academicDocumentId != null) {
                                // Check if academic document exists
                                isAcademicDocumentExists(academicDocumentId) { exists ->
                                    try {
                                        if(exists) {
                                            requireContext().showToast("Academic Already Exist")
                                            return@isAcademicDocumentExists // Exit the callback function
                                        }
                                        if (academicData != null) { // If academic data is not null, insert it into the repository
                                            MainScope().launch(Dispatchers.IO){
                                                try {
                                                    // Insert academic documents into the repository
                                                    AcademicRepository.insertAcademicDocuments(
                                                        academicDocumentId, // Academic document ID
                                                        academicData, // Academic data to be inserted
                                                        {
                                                            // Success callback: When academic documents are successfully added
                                                            academicAddListener?.onAdded(academicDocumentId, academicData) // Call the listener about document added
                                                            dismiss() // Dismiss dialog after document added
                                                        },
                                                        {
                                                            // Error callback: When an exception occurs during insertion
                                                            throw it // Throw the caught exception for handling in the caller
                                                        }
                                                    )
                                                } catch (e: Exception) {
                                                    throw e // Throw the caught exception for handling in the caller
                                                }
                                            }
                                        }

                                    } catch (e: Exception) {
                                        throw e // Throw the caught exception for handling in the caller
                                    }
                                }
                            }

                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }

            }
        }

        try {
            // return the alertdialog box object
            return builder!!.create()
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }


}