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
import com.dk.organizeu.databinding.EditBatchDialogLayoutBinding
import com.dk.organizeu.listener.BatchDocumentListener
import com.dk.organizeu.pojo.BatchPojo
import com.dk.organizeu.repository.BatchRepository
import com.dk.organizeu.utils.UtilFunction.Companion.containsOnlyAllowedCharacters
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore

class EditBatchDialog(val academicDocumentId: String, val semesterDocumentId: String, val classDocumentId:String, val oldBatchPojo: BatchPojo, val position: Int) : AppCompatDialogFragment() {
    private lateinit var db: FirebaseFirestore
    private var batchDocumentListener: BatchDocumentListener? = null

    private lateinit var binding: EditBatchDialogLayoutBinding
    companion object{
        const val TAG = "OrganizeU-EditBatchDialog"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Obtain LayoutInflater from the fragment's context
        val inflater = LayoutInflater.from(requireContext())
        // Inflate the layout for adding a new batch
        val view = inflater.inflate(R.layout.edit_batch_dialog_layout, null)
        // Bind the layout to the view binding batch
        binding = DataBindingUtil.bind(view)!!
        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance()
        var builder: AlertDialog.Builder?=null
        try {
            // Set the batchAddListener if the parentFragment implements EditDocumentListener
            batchDocumentListener = parentFragment as? BatchDocumentListener
            val title = "Edit Batch"
            binding.etBatchName.setText(oldBatchPojo.name)
            builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle(title)


            binding.apply {
                btnClose.setOnClickListener {
                    dismiss()
                }
                btnEdit.setOnClickListener {
                    try {
                        // get batch name from input field
                        val batchName = etBatchName.text.toString().trim().replace(Regex("\\s+")," ")

                        if(!batchName.containsOnlyAllowedCharacters())
                        {
                            tlBatchName.error = "Batch name only allows alphabets, -, _."
                            return@setOnClickListener
                        }
                        tlBatchName.error = null
                        oldBatchPojo.name = batchName
                        BatchRepository.isBatchDocumentExistsByName(academicDocumentId,semesterDocumentId, classDocumentId,oldBatchPojo.name){exists ->
                            if(exists)
                            {
                                requireContext().showToast("Batch Name Already Exist")
                                return@isBatchDocumentExistsByName
                            }
                            BatchRepository.updateBatchDocument(academicDocumentId,semesterDocumentId,classDocumentId,oldBatchPojo){
                                if(it)
                                {
                                    batchDocumentListener!!.onEdited(oldBatchPojo,position)
                                    dismiss()
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


}