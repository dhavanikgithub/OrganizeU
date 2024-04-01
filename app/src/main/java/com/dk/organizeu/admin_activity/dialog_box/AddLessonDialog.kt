package com.dk.organizeu.admin_activity.dialog_box

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.enum_class.RoomType
import com.dk.organizeu.admin_activity.enum_class.SubjectType
import com.dk.organizeu.admin_activity.enum_class.Weekday
import com.dk.organizeu.admin_activity.fragments.timetable.add_timetable.AddTimetableFragment
import com.dk.organizeu.admin_activity.listener.LessonAddListener
import com.dk.organizeu.admin_activity.listener.SubjectAddListener
import com.dk.organizeu.databinding.AddLessonDialogLayoutBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddLessonDialog : AppCompatDialogFragment() {
    private lateinit var binding: AddLessonDialogLayoutBinding

    private lateinit var db: FirebaseFirestore
    private var lessonAddListener: LessonAddListener? = null

    private var selectedSubject:String?=null
    private lateinit var subjectList:ArrayList<String>
    private var subjectAdapter:ArrayAdapter<String>?=null

    private var selectedFaculty:String?=null
    private lateinit var facultyList:ArrayList<String>
    private var facultyAdapter:ArrayAdapter<String>?=null

    private var selectedBatch:String?=null
    private lateinit var batchList:ArrayList<String>
    private var batchAdapter:ArrayAdapter<String>?=null

    private var selectedRoom:String?=null
    private lateinit var roomList:ArrayList<String>
    private var roomAdapter:ArrayAdapter<String>?=null

    private var selectedLessonType:String?=null



    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_lesson_dialog_layout, null)
        binding = AddLessonDialogLayoutBinding.bind(view)
        db = FirebaseFirestore.getInstance()
        lessonAddListener = parentFragment as? LessonAddListener
        subjectList = ArrayList()
        facultyList = ArrayList()
        batchList = ArrayList()
        roomList = ArrayList()
        binding.apply {
            val builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Add Lesson")

            val lessonTypeList = arrayOf(RoomType.CLASS.name,RoomType.LAB.name)
            val lessonTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lessonTypeList)
            lessonTypeACTV.setAdapter(lessonTypeAdapter)

            initSubjectDropDown()
            initFacultyDropDown()

            facultyTIL.isEnabled=false
            lessonTypeTIL.isEnabled=false
            startTimeTIL.isEnabled=false
            endTimeTIL.isEnabled=false
            roomTIL.isEnabled=false
            btnAdd.isEnabled=false



            subjectACTV.setOnItemClickListener { parent, view, position, id ->
                selectedSubject = parent.getItemAtPosition(position).toString()
                clearFacultyDropDown()
                clearLessonTypeDropDown()
                clearBatchDropDown()
                clearStartTime()
                clearEndTime()
                clearRoomDropDown()
                batchTIL.visibility=View.GONE
                facultyTIL.isEnabled=true
                lessonTypeTIL.isEnabled=false
                startTimeTIL.isEnabled=false
                endTimeTIL.isEnabled=false
                roomTIL.isEnabled=false
                btnAdd.isEnabled=false
            }

            facultyACTV.setOnItemClickListener { parent, view, position, id ->
                selectedFaculty = parent.getItemAtPosition(position).toString()
                clearLessonTypeDropDown()
                clearBatchDropDown()
                clearStartTime()
                clearEndTime()
                clearRoomDropDown()
                batchTIL.visibility=View.GONE
                lessonTypeTIL.isEnabled=true
                startTimeTIL.isEnabled=false
                endTimeTIL.isEnabled=false
                roomTIL.isEnabled=false
                btnAdd.isEnabled=false
            }

            lessonTypeACTV.setOnItemClickListener { parent, view, position, id ->
                selectedLessonType = parent.getItemAtPosition(position).toString()
                clearBatchDropDown()
                clearStartTime()
                clearEndTime()
                clearRoomDropDown()

                if(selectedLessonType.equals(RoomType.LAB.name,true))
                {
                    initBatchDropDown()
                    batchTIL.visibility = View.VISIBLE
                    startTimeTIL.isEnabled=false
                    endTimeTIL.isEnabled=false
                    btnAdd.isEnabled=false

                }
                else{
                    batchTIL.visibility = View.GONE
                    startTimeTIL.isEnabled=true
                    endTimeTIL.isEnabled=false
                    btnAdd.isEnabled=false
                }


            }
            batchACTV.setOnItemClickListener { parent, view, position, id ->
                selectedBatch = parent.getItemAtPosition(position).toString()
                clearStartTime()
                clearEndTime()
                clearRoomDropDown()
                startTimeTIL.isEnabled=true
                endTimeTIL.isEnabled=false
                roomTIL.isEnabled=false
                btnAdd.isEnabled=false
            }

            roomACTV.setOnItemClickListener { parent, view, position, id ->
                selectedRoom = parent.getItemAtPosition(position).toString()
                btnAdd.isEnabled=true
            }


            btnClose.setOnClickListener {
                dismiss()
            }
            btnAdd.setOnClickListener {
                if(selectedSubject!=null &&
                    selectedFaculty!=null &&
                    selectedLessonType != null &&
                    selectedRoom != null &&
                    startTimeET.text.toString().isNotBlank() &&
                    startTimeET.text.toString().isNotEmpty() &&
                    endTimeEL.text.toString().isNotBlank() &&
                    endTimeEL.text.toString().isNotEmpty() &&
                    ((selectedLessonType.equals(RoomType.LAB.name) && selectedBatch!=null) || (selectedLessonType.equals(RoomType.CLASS.name))))
                {
                    AddTimetableFragment.apply {
                        db.collection("subject")
                            .document(selectedSubject!!)
                            .get()
                            .addOnSuccessListener {subjectDoc->
                                db.collection("room")
                                    .document(selectedRoom!!)
                                    .get()
                                    .addOnSuccessListener {roomDoc->
                                        db.collection("academic")
                                            .document("${academicYear}_${academicType}")
                                            .collection("semester")
                                            .document(semesterNumber)
                                            .collection("class")
                                            .document(className)
                                            .collection("timetable")
                                            .document(Weekday.getWeekdayNameByNumber((selectedTab+1)))
                                            .collection("weekday")

                                            .document()
                                            .set(hashMapOf(
                                                "class_name" to className,
                                                "subject_name" to selectedSubject,
                                                "subject_code" to subjectDoc.get("code"),
                                                "location" to roomDoc.get("location"),
                                                "start_time" to startTimeET.text.toString(),
                                                "end_time" to endTimeEL.text.toString(),
                                                "faculty" to selectedFaculty,
                                                "type" to selectedLessonType,
                                                "batch" to selectedBatch.toString(),
                                                "duration" to calculateLessonDuration(startTimeET.text.toString(),endTimeEL.text.toString())
                                            ))
                                            .addOnSuccessListener {
                                                btnClose.callOnClick()
                                            }
                                    }

                            }


                    }
                }
            }
            startTimeET.setOnClickListener {
                showTimePicker(startTimeET)

            }

            startTimeET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    clearEndTime()
                    clearRoomDropDown()
                    endTimeTIL.isEnabled = startTimeET.text.toString()!=""
                    roomTIL.isEnabled=false
                    btnAdd.isEnabled=false
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            endTimeEL.setOnClickListener {
                showTimePicker(endTimeEL)
            }

            endTimeEL.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    clearRoomDropDown()
                    if(endTimeEL.text.toString()!="")
                    {
                        initRoomDropDown()
                        roomTIL.isEnabled=true
                    }
                    else{
                        roomTIL.isEnabled=false
                    }
                    btnAdd.isEnabled=false
                }

                override fun afterTextChanged(s: Editable?) {}
            })


            return builder.create()
        }
    }

    private fun clearFacultyDropDown()
    {
        binding.apply {
            facultyACTV.setText("")
            selectedFaculty=null
        }
    }

    private fun clearLessonTypeDropDown()
    {
        binding.apply {
            lessonTypeACTV.setText("")
            selectedLessonType=null
        }
    }

    private fun clearBatchDropDown()
    {
        binding.apply {
            batchACTV.setText("")
            selectedBatch=null
        }
    }


    private fun clearRoomDropDown()
    {
        binding.apply {
            roomACTV.setText("")
            selectedRoom=null
        }
    }

    private fun clearStartTime()
    {
        binding.apply {
            startTimeET.setText("")
        }
    }


    private fun clearEndTime()
    {
        binding.apply {
            endTimeEL.setText("")
            endTimeEL.error=null
        }
    }

    fun calculateLessonDuration(startTime: String, endTime: String): String {
        // Define the date format
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        try {
            // Parse the start and end time strings
            val startTimeDate = timeFormat.parse(startTime)
            val endTimeDate = timeFormat.parse(endTime)

            if (startTimeDate != null && endTimeDate != null) {
                // Calculate the difference in milliseconds
                val durationInMillis = endTimeDate.time - startTimeDate.time

                // Convert milliseconds to minutes
                val durationInMinutes = durationInMillis / (1000 * 60)

                // Calculate hours and minutes from the total minutes
                val hours = durationInMinutes / 60
                val minutes = durationInMinutes % 60

                // Return the formatted duration string
                return String.format("%02d:%02d", hours, minutes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return an empty string if there's any error
        return ""
    }

    private fun initSubjectDropDown()
    {
        binding.apply {
            AddTimetableFragment.apply {
                subjectTIL.isEnabled=false
                subjectList.clear()
                if(academicYear!=null && academicType!=null && semesterNumber!=null && className!=null){
                    db.collection("subject")
                        .get()
                        .addOnSuccessListener {documents->
                            for(document in documents)
                            {
                                subjectList.add(document.id)
                            }
                            subjectAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,subjectList)
                            subjectACTV.setAdapter(subjectAdapter)
                            subjectACTV.isEnabled=true
                        }
                }
            }
        }
    }

    private fun initFacultyDropDown()
    {
        binding.apply {
            facultyList.clear()
            db.collection("faculty")
                .get()
                .addOnSuccessListener {documents->
                    for(document in documents)
                    {
                        facultyList.add(document.id)
                    }
                    facultyAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,facultyList)
                    facultyACTV.setAdapter(facultyAdapter)
                }
        }
    }

    private fun initBatchDropDown()
    {
        binding.apply {
            batchTIL.isEnabled=false
            batchList.clear()
            AddTimetableFragment.apply {
                db.collection("academic")
                    .document("${academicYear}_$academicType")
                    .collection("semester")
                    .document(semesterNumber)
                    .collection("class")
                    .document(className)
                    .collection("batch")
                    .get()
                    .addOnSuccessListener {documents->
                        for (document in documents)
                        {
                            batchList.add(document.id)
                        }
                        batchAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,batchList)
                        batchACTV.setAdapter(batchAdapter)
                        batchTIL.isEnabled=true
                    }
            }
        }
    }

    private fun initRoomDropDown()
    {
        binding.apply {
            roomTIL.isEnabled=false
            roomList.clear()
            db.collection("room")
                .whereEqualTo("type",selectedLessonType)
                .get()
                .addOnSuccessListener {documents->
                    for(document in documents)
                    {
                        roomList.add(document.id)
                    }
                    roomAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,roomList)
                    roomACTV.setAdapter(roomAdapter)
                    roomTIL.isEnabled=true
                }
        }
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
        binding.apply {
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