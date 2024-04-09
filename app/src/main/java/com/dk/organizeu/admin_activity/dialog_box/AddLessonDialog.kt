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
import androidx.appcompat.app.AppCompatDialogFragment
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.enum_class.RoomType
import com.dk.organizeu.admin_activity.enum_class.Weekday
import com.dk.organizeu.admin_activity.fragments.timetable.add_lesson.AddLessonFragment
import com.dk.organizeu.admin_activity.listener.LessonAddListener
import com.dk.organizeu.databinding.AddLessonDialogLayoutBinding
import com.dk.organizeu.repository.BatchRepository
import com.dk.organizeu.repository.FacultyRepository
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.LessonRepository.Companion.isLessonDocumentExistByField
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.repository.RoomRepository.Companion.getRoomDocumentsByField
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.utils.UtilFunction.Companion.calculateLessonDuration
import com.dk.organizeu.utils.UtilFunction.Companion.validateTime
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddLessonDialog(private val listener: LessonListener) : AppCompatDialogFragment() {
    private lateinit var binding: AddLessonDialogLayoutBinding

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

    interface LessonListener {
        fun onAddLesson()
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_lesson_dialog_layout, null)
        binding = AddLessonDialogLayoutBinding.bind(view)
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
                    MainScope().launch(Dispatchers.IO) {
                        AddLessonFragment.apply {
                            val subjectDocumentId = selectedSubject!!
                            val roomDocumentId = selectedRoom!!
                            val subjectData = SubjectRepository.subjectDocumentToSubjectObj(SubjectRepository.getSubjectDocumentById(subjectDocumentId)!!)
                            val roomData = RoomRepository.roomDocumentToRoomObj(RoomRepository.getRoomDocumentById(roomDocumentId)!!)

                            val dataSet = hashMapOf(
                                "class_name" to className,
                                "subject_name" to selectedSubject!!,
                                "subject_code" to subjectData.code,
                                "location" to "$selectedRoom - ${roomData.location}",
                                "start_time" to startTimeET.text.toString(),
                                "end_time" to endTimeEL.text.toString(),
                                "faculty" to selectedFaculty!!,
                                "type" to selectedLessonType!!,
                                "batch" to selectedBatch.toString(),
                                "duration" to calculateLessonDuration(startTimeET.text.toString(),endTimeEL.text.toString())
                            )
                            val academicDocumentId = "${academicYear}_${academicType}"
                            val semesterDocumentId = semesterNumber
                            val classDocumentId = className
                            val timetableDocumentId = Weekday.getWeekdayNameByNumber((selectedTab+1))
                            isLessonDocumentExistByField(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,"start_time",startTimeET.text.toString()) {
                                LessonRepository.insertLessonDocument(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,dataSet,{
                                    listener.onAddLesson()
                                    btnClose.callOnClick()
                                },{
                                    btnClose.callOnClick()
                                })
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



    private fun initSubjectDropDown()
    {
        binding.apply {
            AddLessonFragment.apply {
                subjectTIL.isEnabled=false
                MainScope().launch(Dispatchers.IO)
                {
                    subjectList.clear()
                    if(academicYear!=null && academicType!=null && semesterNumber!=null && className!=null){
                        val documents = SubjectRepository.getAllSubjectDocuments()
                        for(document in documents)
                        {
                            subjectList.add(document.id)
                        }
                        withContext(Dispatchers.Main)
                        {
                            subjectAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,subjectList)
                            subjectACTV.setAdapter(subjectAdapter)
                            subjectACTV.isEnabled=true
                        }
                    }
                }
            }
        }
    }

    private fun initFacultyDropDown()
    {
        binding.apply {
            MainScope().launch(Dispatchers.IO)
            {
                facultyList.clear()
                val documents = FacultyRepository.getAllFacultyDocuments()
                for(document in documents)
                {
                    facultyList.add(document.id)
                }
                withContext(Dispatchers.Main)
                {
                    facultyAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,facultyList)
                    facultyACTV.setAdapter(facultyAdapter)
                }
            }
        }
    }

    private fun initBatchDropDown()
    {
        binding.apply {
            batchTIL.isEnabled=false
            MainScope().launch(Dispatchers.IO) {
                batchList.clear()
                AddLessonFragment.apply {
                    val academicDocumentId = "${academicYear}_$academicType"
                    val semesterDocumentId = semesterNumber
                    val classDocumentId = className
                    if(academicDocumentId!=null && semesterDocumentId!=null && classDocumentId!=null)
                    {
                        val documents = BatchRepository.getAllBatchDocuments(academicDocumentId, semesterDocumentId, classDocumentId)
                        for (document in documents)
                        {
                            batchList.add(document.id)
                        }
                    }
                    withContext(Dispatchers.Main)
                    {
                        batchAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,batchList)
                        batchACTV.setAdapter(batchAdapter)
                        batchTIL.isEnabled=true
                    }
                }
            }
        }
    }

    private fun initRoomDropDown()
    {
        binding.apply {
            roomTIL.isEnabled=false
            MainScope().launch(Dispatchers.IO)
            {
                roomList.clear()
                val documents = getRoomDocumentsByField("type",selectedLessonType!!)
                for(document in documents)
                {
                    roomList.add(document.id)
                }
                withContext(Dispatchers.Main)
                {
                    roomAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,roomList)
                    roomACTV.setAdapter(roomAdapter)
                    roomTIL.isEnabled=true
                }
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
            binding.apply {
                val startTimeString = startTimeET.text.toString().trim()
                val endTimeString = endTimeEL.text.toString().trim()
                if(validateTime(startTimeString, endTimeString))
                {
                    endTimeEL.error = null
                }
                else{
                    endTimeEL.setText("")
                    endTimeEL.error = "End time must be after start time"
                }
            }
        }
    }

}