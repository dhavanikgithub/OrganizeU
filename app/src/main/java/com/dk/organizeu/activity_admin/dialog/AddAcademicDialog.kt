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
import com.dk.organizeu.listener.AcademicDocumentListener
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.repository.AcademicRepository
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.dk.organizeu.utils.Validation.Companion.isItemSelected
import java.util.Calendar

class AddAcademicDialog() : AppCompatDialogFragment() {
    private var academicDocumentListener: AcademicDocumentListener? = null
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
        academicDocumentListener = parentFragment as? AcademicDocumentListener
        // Create object of AlertDialog box
        builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Academic")
        binding.apply {
            try {
                // Prepare academic year list for Initialize academic year drop down
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

                val academicYears = mutableListOf<String>()

                // Prepare academic year list from now to up to 2 year before
                if(currentMonth<=5)
                {
                    for (year in currentYear..currentYear + 1) {
                        academicYears.add("$year-${year + 1}")
                    }
                    academicYears.add(0,"${currentYear-1}-$currentYear")
                }
                else{
                    for (year in currentYear..currentYear + 2) {
                        academicYears.add("$year-${year + 1}")
                    }
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
                            val academicYear = actAcademicYear.text.toString()
                            val academicType = if(chipEven.isChecked) chipEven.text.toString() else chipOdd .text.toString()

                            val newAcademicPojo = AcademicPojo(
                                year = academicYear,
                                type = academicType
                            )

                            // Check if academic document exists
                            AcademicRepository.isAcademicDocumentExistsByYearAndType(newAcademicPojo) { exists ->
                                try {
                                    if(exists) {
                                        requireContext().showToast("Academic Already Exist")
                                        return@isAcademicDocumentExistsByYearAndType // Exit the callback function
                                    }
                                    // Insert academic documents into the repository
                                    AcademicRepository.insertAcademicDocuments(newAcademicPojo,
                                        {
                                            // Success callback: When academic documents are successfully added
                                            academicDocumentListener?.onAdded(newAcademicPojo) // Call the listener about document added
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