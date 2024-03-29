package com.dk.organizeu.admin_activity.dialog_box

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.enum_class.RoomType
import com.dk.organizeu.admin_activity.enum_class.SubjectType
import com.dk.organizeu.admin_activity.listener.LessonAddListener
import com.dk.organizeu.admin_activity.listener.SubjectAddListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar

class AddLessonDialog() : AppCompatDialogFragment() {
    private lateinit var subjectACTV: AutoCompleteTextView
    private lateinit var facultyACTV: AutoCompleteTextView
    private lateinit var lessonTypeACTV: AutoCompleteTextView
    private lateinit var batchACTV: AutoCompleteTextView
    private lateinit var roomACTV: AutoCompleteTextView
    private lateinit var startTimeET: TextInputEditText
    private lateinit var endTimeEL: TextInputEditText
    private lateinit var addButton: MaterialButton
    private lateinit var closeButton: MaterialButton

    private lateinit var db: FirebaseFirestore
    private var lessonAddListener: LessonAddListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_lesson_dialog_layout, null)
        db = FirebaseFirestore.getInstance()
        lessonAddListener = parentFragment as? LessonAddListener
        subjectACTV = view.findViewById(R.id.subjectACTV)
        facultyACTV = view.findViewById(R.id.facultyACTV)
        lessonTypeACTV = view.findViewById(R.id.lessonTypeACTV)
        batchACTV = view.findViewById(R.id.batchACTV)
        roomACTV = view.findViewById(R.id.roomACTV)


        endTimeEL = view.findViewById(R.id.endTimeEL)
        startTimeET = view.findViewById(R.id.startTimeET)
        addButton = view.findViewById(R.id.btnAdd)
        closeButton = view.findViewById(R.id.btnClose)

        val lessonTypeList = arrayOf(RoomType.CLASS.name,RoomType.LAB.name)
        val lessonTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lessonTypeList)
        lessonTypeACTV.setAdapter(lessonTypeAdapter)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add Lesson")

        closeButton.setOnClickListener {
            dismiss()
        }
        addButton.setOnClickListener {

        }
        startTimeET.setOnClickListener {
            showTimePicker(startTimeET)
        }

        endTimeEL.setOnClickListener {
            showTimePicker(endTimeEL)
        }
        return builder.create()
    }

    private fun showTimePicker(textInput: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->

                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                textInput.setText(selectedTime)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
        timePickerDialog.setOnDismissListener {
            validateTime()
        }
    }

    private fun validateTime() {
        val startTimeString = startTimeET.text.toString()
        val endTimeString = endTimeEL.text.toString()

        if (startTimeString.isNotEmpty() && endTimeString.isNotEmpty()) {
            val startTime = SimpleDateFormat("HH:mm").parse(startTimeString)
            val endTime = SimpleDateFormat("HH:mm").parse(endTimeString)

            if (startTime != null && endTime != null) {
                if (endTime.before(startTime)) {
                    endTimeEL.setText("")
                    endTimeEL.error = "End time must be after start time"
                } else {
                    endTimeEL.error = null
                }
            }
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