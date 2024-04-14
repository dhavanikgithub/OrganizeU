package com.dk.organizeu.admin_activity.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.ashiqurrahman.rangedtimepickerdialog.library.TimeRangePickerDialog
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.fragments.timetable.add_lesson.AddLessonFragment
import com.dk.organizeu.databinding.AddLessonDialogLayoutBinding
import com.dk.organizeu.enum_class.RoomType
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.*
import com.dk.organizeu.repository.LessonRepository.Companion.isLessonDocumentConflict
import com.dk.organizeu.repository.RoomRepository.Companion.getRoomDocumentsByField
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.calculateLessonDuration
import com.dk.organizeu.utils.UtilFunction.Companion.validateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLessonDialog(private val listener: LessonListener) : AppCompatDialogFragment() {
    private lateinit var binding: AddLessonDialogLayoutBinding

    private var lessonAddListener: AddDocumentListener? = null

    private var selectedTime:String?=null

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
        fun onConflict()
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_lesson_dialog_layout, null)
        binding = AddLessonDialogLayoutBinding.bind(view)
        lessonAddListener = parentFragment as? AddDocumentListener
        subjectList = ArrayList()
        facultyList = ArrayList()
        batchList = ArrayList()
        roomList = ArrayList()
        binding.apply {
            val builder = AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Add Lesson")

            val lessonTypeList = arrayOf(RoomType.CLASS.name, RoomType.LAB.name)
            val lessonTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lessonTypeList)
            actLessonType.setAdapter(lessonTypeAdapter)

            initSubjectDropDown()
            initFacultyDropDown()

            initBatchDropDown()


            actSubject.setOnItemClickListener { parent, view, position, id ->
                if(!validateSubject())
                {
                    return@setOnItemClickListener
                }
                selectedSubject = parent.getItemAtPosition(position).toString()
                tlBatch.visibility=View.GONE
            }

            actFaculty.setOnItemClickListener { parent, view, position, id ->
                if(!validateFaculty())
                {
                    return@setOnItemClickListener
                }
                selectedFaculty = parent.getItemAtPosition(position).toString()
                tlBatch.visibility=View.GONE
            }

            actLessonType.setOnItemClickListener { parent, view, position, id ->
                if(!validateLessonType())
                {
                    return@setOnItemClickListener
                }
                selectedLessonType = parent.getItemAtPosition(position).toString()

                if(selectedLessonType.equals(RoomType.LAB.name,true))
                {
                    tlBatch.visibility = View.VISIBLE
                }
                else{
                    tlBatch.visibility = View.GONE
                }
                initRoomDropDown()
            }
            actBatch.setOnItemClickListener { parent, view, position, id ->
                if(!validateBatch())
                {
                    return@setOnItemClickListener
                }
                selectedBatch = parent.getItemAtPosition(position).toString()
            }

            actRoom.setOnItemClickListener { parent, view, position, id ->
                if(!validateRoom())
                {
                    return@setOnItemClickListener
                }
                selectedRoom = parent.getItemAtPosition(position).toString()
            }

            btnClose.setOnClickListener {
                dismiss()
            }
            btnAdd.setOnClickListener {
                if(validateFields())
                {
                    if(selectedLessonType.equals(RoomType.LAB.name))
                    {
                        if(!validateBatch())
                        {
                            return@setOnClickListener
                        }
                    }
                    MainScope().launch(Dispatchers.IO) {
                        AddLessonFragment.apply {
                            val subjectDocumentId = selectedSubject!!
                            val roomDocumentId = selectedRoom!!
                            val subjectData = SubjectRepository.subjectDocumentToSubjectObj(SubjectRepository.getSubjectDocumentById(subjectDocumentId)!!)
                            val roomData = RoomRepository.roomDocumentToRoomObj(RoomRepository.getRoomDocumentById(roomDocumentId)!!)
                            val selectedLessonTime = selectedTime!!.split(" - ")
                            val dataSet = hashMapOf(
                                WeekdayCollection.CLASS_NAME.displayName to className,
                                WeekdayCollection.SUBJECT_NAME.displayName to selectedSubject!!,
                                WeekdayCollection.SUBJECT_CODE.displayName to subjectData.code,
                                WeekdayCollection.LOCATION.displayName to "$selectedRoom - ${roomData.location}",
                                WeekdayCollection.START_TIME.displayName to selectedLessonTime[0],
                                WeekdayCollection.END_TIME.displayName to selectedLessonTime[1],
                                WeekdayCollection.FACULTY_NAME.displayName to selectedFaculty!!,
                                WeekdayCollection.TYPE.displayName to selectedLessonType!!,
                                WeekdayCollection.BATCH.displayName to selectedBatch.toString(),
                                WeekdayCollection.DURATION.displayName to calculateLessonDuration(selectedLessonTime[0],selectedLessonTime[1])
                            )
                            val academicDocumentId = "${academicYear}_${academicType}"
                            val semesterDocumentId = semesterNumber
                            val classDocumentId = className
                            val timetableDocumentId = Weekday.getWeekdayNameByNumber((selectedTab+1))

                            isLessonDocumentConflict(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,selectedLessonTime[0],selectedLessonTime[1],selectedFaculty!!) {
                                if(!it)
                                {
                                    LessonRepository.insertLessonDocument(academicDocumentId,semesterDocumentId,classDocumentId,timetableDocumentId,dataSet,{
                                        listener.onAddLesson()
                                        btnClose.callOnClick()
                                    },{
                                        btnClose.callOnClick()
                                    })
                                }
                                else{
                                    listener.onConflict()
                                }

                            }
                        }
                    }

                }
            }

            txtSelectLessonTime.setOnClickListener {
                showTimePicker(txtSelectLessonTime)
            }

            return builder.create()
        }
    }


    private fun initSubjectDropDown()
    {
        binding.apply {
            AddLessonFragment.apply {
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
                            actSubject.setAdapter(subjectAdapter)
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
                    actFaculty.setAdapter(facultyAdapter)
                }
            }
        }
    }

    private fun initBatchDropDown()
    {
        binding.apply {
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
                        actBatch.setAdapter(batchAdapter)
                    }
                }
            }
        }
    }

    private fun initRoomDropDown()
    {
        binding.apply {
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
                    actRoom.setAdapter(roomAdapter)
                }
            }
        }
    }

    fun validateFields(): Boolean
    {
        validateSubject()
        validateFaculty()
        validateLessonType()
        validateRoom()
        validateLessonTime()
        return (binding.tlSubject.error == null)
                && (binding.tlFaculty.error == null)
                && (binding.tlLessonType.error == null)
                && (binding.tlRoom.error == null)
                && (binding.tlSelectLessonTime.error == null)
    }


    private fun showTimePicker(textInput: TextView) {

        val dialog = TimeRangePickerDialog(
            startLabel = "START",
            endLabel = "END",
            is24HourView = false,
            onPickedTimeTime = object : TimeRangePickerDialog.OnPickedTimeRange {
                override fun onPickedTime(
                    startHour: Int,
                    startMinute: Int,
                    endHour: Int,
                    endMinute: Int
                ) {
                    val selectedStartTime = UtilFunction.convertTo12HourFormat(startHour,startMinute)
                    val selectedEndTime = UtilFunction.convertTo12HourFormat(endHour,endMinute)

                    if(validateTime(selectedStartTime, selectedEndTime)) {
                        binding.tlSelectLessonTime.error = null
                        textInput.text = "$selectedStartTime - $selectedEndTime"
                    }
                    else {
                        textInput.text = ""
                        binding.tlSelectLessonTime.error = "End time must be after start time"
                    }
                }

            }
        )

        dialog.isCancelable = false
        dialog.show(requireActivity().supportFragmentManager, "Select Lesson Time")
    }

    fun validateSubject(): Boolean {
        var isValid = true
        binding.tlSubject.error = null
        selectedSubject = binding.actSubject.text.toString()
        if(selectedSubject!!.isEmpty() || selectedSubject!!.isBlank())
        {
            isValid = false
            binding.tlSubject.error = "Required"
        }
        return isValid
    }

    fun validateFaculty(): Boolean {
        var isValid = true
        binding.tlFaculty.error = null
        selectedFaculty = binding.actFaculty.text.toString()
        if(selectedFaculty!!.isEmpty() || selectedFaculty!!.isBlank())
        {
            isValid = false
            binding.tlFaculty.error = "Required"
        }
        return isValid
    }

    fun validateLessonType(): Boolean {
        var isValid = true
        binding.tlLessonType.error = null
        selectedLessonType = binding.actLessonType.text.toString()
        if(selectedLessonType!!.isEmpty() || selectedLessonType!!.isBlank())
        {
            isValid = false
            binding.tlLessonType.error = "Required"
        }
        return isValid
    }

    fun validateRoom(): Boolean {
        var isValid = true
        binding.tlRoom.error = null
        selectedRoom = binding.actRoom.text.toString()
        if(selectedRoom!!.isEmpty() || selectedRoom!!.isBlank())
        {
            isValid = false
            binding.tlRoom.error = "Required"
        }
        return isValid
    }

    fun validateLessonTime(): Boolean {
        var isValid = true
        binding.tlSelectLessonTime.error = null
        selectedTime = binding.txtSelectLessonTime.text.toString()
        if(selectedTime!!.isEmpty() || selectedTime!!.isBlank() || selectedTime == null)
        {
            binding.tlSelectLessonTime.error = "Required"
            isValid = false
        }
        return isValid
    }

    fun validateBatch(): Boolean {
        var isValid = true
        binding.actBatch.error = null
        selectedBatch = binding.actBatch.text.toString()
        if(selectedBatch!!.isEmpty() || selectedBatch!!.isBlank())
        {
            isValid = false
            binding.tlBatch.error = "Required"
        }
        return isValid
    }


}