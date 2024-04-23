package com.dk.organizeu.activity_admin.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.ashiqurrahman.rangedtimepickerdialog.library.TimeRangePickerDialog
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.fragments.timetable.add_lesson.AddLessonFragment
import com.dk.organizeu.databinding.AddLessonDialogLayoutBinding
import com.dk.organizeu.enum_class.RoomType
import com.dk.organizeu.enum_class.Weekday
import com.dk.organizeu.firebase.key_mapping.WeekdayCollection
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.repository.BatchRepository
import com.dk.organizeu.repository.FacultyRepository
import com.dk.organizeu.repository.LessonRepository
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.repository.RoomRepository.Companion.getRoomDocumentsByField
import com.dk.organizeu.repository.SubjectRepository
import com.dk.organizeu.utils.TimeConverter.Companion.convert12HourTo24Hour
import com.dk.organizeu.utils.TimeConverter.Companion.convertTo12HourFormat
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.dk.organizeu.utils.Validation.Companion.validateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
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

    /**
     * Interface for handling lesson events.
     */
    interface LessonListener {
        /**
         * Called when a new lesson is added successfully.
         */
        fun onAddLesson()

        /**
         * Called when a lesson conflict is detected while adding.
         */
        fun onConflict()
    }

    companion object{
        const val TAG = "OrganizeU-AddLessonDialog"
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize a LayoutInflater with the context of the current fragment
        val inflater = LayoutInflater.from(requireContext())

        // Inflate the layout for the add lesson dialog
        val view = inflater.inflate(R.layout.add_lesson_dialog_layout, null)

        // Bind the layout to the view binding class
        binding = AddLessonDialogLayoutBinding.bind(view)

        // Set the lessonAddListener if the parentFragment implements AddDocumentListener
        lessonAddListener = parentFragment as? AddDocumentListener
        subjectList = ArrayList()
        facultyList = ArrayList()
        batchList = ArrayList()
        roomList = ArrayList()
        binding.apply {
            var builder:AlertDialog.Builder? = null

            try {
                // Create an AlertDialog.Builder instance with the context of the current fragment
                builder = AlertDialog.Builder(requireContext())
                    .setView(view) // Set the view of the dialog to the inflated view
                    .setTitle("Add Lesson") // Set the title of the dialog

                // Define the list of lesson types (e.g., CLASS and LAB)
                val lessonTypeList = arrayOf(RoomType.CLASS.name, RoomType.LAB.name)
                // Create an ArrayAdapter for the lesson type drop-down list
                val lessonTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lessonTypeList)
                // Set the adapter for the lesson type drop-down
                actLessonType.setAdapter(lessonTypeAdapter)

                // Initialize and populate the drop-down lists for subject, faculty, and batch
                initSubjectDropDown()
                initFacultyDropDown()
                initBatchDropDown()
            } catch (e: Exception) {
                // Log any exceptions that occur
                Log.e(TAG, e.message.toString())
                // Display an unexpected error message to the user
                requireContext().unexpectedErrorMessagePrint(e)
            }


            actSubject.setOnItemClickListener { parent, view, position, id ->
                try {
                    // Validate the subject input. If it's not valid, return without further processing.
                    if(!validateSubject())
                    {
                        return@setOnItemClickListener
                    }
                    // Get the selected subject from the parent adapter
                    selectedSubject = parent.getItemAtPosition(position).toString()
                    tlBatch.visibility=View.GONE
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }

            actFaculty.setOnItemClickListener { parent, view, position, id ->
                try {
                    // Validate the faculty input. If it's not valid, return without further processing.
                    if(!validateFaculty())
                    {
                        return@setOnItemClickListener
                    }
                    // Get the selected faculty from the parent adapter
                    selectedFaculty = parent.getItemAtPosition(position).toString()
                    tlBatch.visibility=View.GONE
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }

            actLessonType.setOnItemClickListener { parent, view, position, id ->
                try {
                    // Validate the lesson type input. If it's not valid, return without further processing.
                    if(!validateLessonType())
                    {
                        return@setOnItemClickListener
                    }
                    // Get the selected lesson type from the parent adapter
                    selectedLessonType = parent.getItemAtPosition(position).toString()

                    // Check if the selected lesson type is a lab
                    if(selectedLessonType.equals(RoomType.LAB.name,true))
                    {
                        // If it's a lab, make the batch drop down visible
                        tlBatch.visibility = View.VISIBLE
                    }
                    else{
                        // If it's not a lab, hide the batch drop down
                        tlBatch.visibility = View.GONE
                    }
                    // Initialize the room drop-down list based on the selected lesson type
                    initRoomDropDown()
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }

            actBatch.setOnItemClickListener { parent, view, position, id ->
                try {
                    // Validate the batch input. If it's not valid, return without further processing.
                    if(!validateBatch())
                    {
                        return@setOnItemClickListener
                    }
                    // Get the selected batch from the parent adapter
                    selectedBatch = parent.getItemAtPosition(position).toString()
                }  catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }

            actRoom.setOnItemClickListener { parent, view, position, id ->
                try {
                    // Validate the room input. If it's not valid, return without further processing.
                    if(!validateRoom())
                    {
                        return@setOnItemClickListener
                    }
                    // Get the selected room from the parent adapter
                    selectedRoom = parent.getItemAtPosition(position).toString()
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }

            btnClose.setOnClickListener {
                dismiss()
            }

            btnAdd.setOnClickListener {
                try {
                    // Validate all input fields. If validation fails, return without further processing.
                    if(!validateFields())
                    {
                        return@setOnClickListener
                    }
                    // If the lesson type is a LAB
                    // Validate the batch input. If it's not valid, return without further processing.
                    if(selectedLessonType.equals(RoomType.LAB.name) && !validateBatch())
                    {
                        return@setOnClickListener
                    }

                    MainScope().launch(Dispatchers.IO) {
                        AddLessonFragment.apply {
                            try {
                                // Get the IDs of the selected subject and room
                                val subjectDocumentId = selectedSubject!!
                                val roomDocumentId = selectedRoom!!

                                // Retrieve data objects for the selected subject and room from repositories
                                val subjectData = SubjectRepository.subjectDocumentToSubjectObj(SubjectRepository.getSubjectDocumentById(subjectDocumentId)!!)
                                val roomData = RoomRepository.roomDocumentToRoomObj(RoomRepository.getRoomDocumentById(roomDocumentId)!!)

                                // Split the selected lesson time into start and end times
                                // input example 10:00 AM - 11:00 AM
                                // output ['10:00 AM','11:00 AM']
                                val selectedLessonTime = selectedTime!!.split(" - ")


                                // Generate unique request codes for muting, unmuting, and notifications
                                val muteRequestCode = System.currentTimeMillis().toInt()
                                delay(100)
                                val unmuteRequestCode = System.currentTimeMillis().toInt()
                                delay(100)
                                val notificationCode = System.currentTimeMillis().toInt()

                                // Create a data set containing information about the lesson
                                val dataSet = hashMapOf(
                                    WeekdayCollection.CLASS_NAME.displayName to className,
                                    WeekdayCollection.SUBJECT_NAME.displayName to selectedSubject!!,
                                    WeekdayCollection.SUBJECT_CODE.displayName to subjectData.code,
                                    WeekdayCollection.LOCATION.displayName to "$selectedRoom - ${roomData.location}",
                                    WeekdayCollection.START_TIME.displayName to selectedLessonTime[0].convert12HourTo24Hour(),
                                    WeekdayCollection.END_TIME.displayName to selectedLessonTime[1].convert12HourTo24Hour(),
                                    WeekdayCollection.FACULTY_NAME.displayName to selectedFaculty!!,
                                    WeekdayCollection.TYPE.displayName to selectedLessonType!!,
                                    WeekdayCollection.BATCH.displayName to selectedBatch.toString(),
                                    WeekdayCollection.DURATION.displayName to UtilFunction.calculateLessonDuration(
                                        selectedLessonTime[0],
                                        selectedLessonTime[1]
                                    ),
                                    WeekdayCollection.MUTE_REQUEST_CODE.displayName to muteRequestCode.toString(),
                                    WeekdayCollection.UNMUTE_REQUEST_CODE.displayName to unmuteRequestCode.toString(),
                                    WeekdayCollection.NOTIFICATION_CODE.displayName to notificationCode.toString()
                                )

                                // Get the IDs for the academic year, semester, class, and timetable
                                val academicDocumentId = "${academicYear}_${academicType}"
                                val semesterDocumentId = semesterNumber
                                val classDocumentId = className
                                val timetableDocumentId = Weekday.getWeekdayNameByNumber((selectedTab+1))


                                // Check if there's a conflict with existing lesson documents
                                LessonRepository.isLessonDocumentConflict(
                                    academicDocumentId,
                                    semesterDocumentId,
                                    classDocumentId,
                                    timetableDocumentId,
                                    selectedLessonTime[0],
                                    selectedLessonTime[1],
                                    selectedFaculty!!,
                                    "$selectedRoom - ${roomData.location}"
                                ) {
                                    try {
                                        if (!it) {
                                            // If there's no conflict, insert the lesson document
                                            LessonRepository.insertLessonDocument(
                                                academicDocumentId,
                                                semesterDocumentId,
                                                classDocumentId,
                                                timetableDocumentId,
                                                dataSet,
                                                {
                                                    // Invoke the onAddLesson listener callback
                                                    listener.onAddLesson()
                                                    // Close the dialog
                                                    dismiss()
                                                },
                                                {
                                                    // Close the dialog
                                                    dismiss()
                                                })
                                        } else {
                                            // If there's a conflict, invoke the onConflict listener callback
                                            listener.onConflict()
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, e.message.toString())
                                        requireContext().unexpectedErrorMessagePrint(e)
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
            }

            txtSelectLessonTime.setOnClickListener {
                try {
                    // Show the time picker dialog
                    showTimePicker(txtSelectLessonTime)
                } catch (e: Exception) {
                    // Log any unexpected exceptions that occur
                    Log.e(TAG,e.message.toString())
                    // Display an unexpected error message to the user
                    requireContext().unexpectedErrorMessagePrint(e)
                }
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


    /**
     * Initializes the subject drop-down list.
     * This function populates the subject drop-down list with subjects retrieved from the repository.
     */
    private fun initSubjectDropDown()
    {
        binding.apply {
            AddLessonFragment.apply {
                MainScope().launch(Dispatchers.IO)
                {
                    try {
                        // Clear the subject list to start fresh
                        subjectList.clear()
                        if(academicYear!=null && academicType!=null && semesterNumber!=null && className!=null){

                            // Retrieve all subject documents from the repository
                            val documents = SubjectRepository.getAllSubjectDocuments()

                            // Iterate through the documents and add them to the subject list
                            for(document in documents)
                            {
                                subjectList.add(document.id)
                            }
                            // Update the UI on the main thread
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    // Create an ArrayAdapter for the subject drop-down
                                    subjectAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,subjectList)
                                    // Set the adapter
                                    actSubject.setAdapter(subjectAdapter)
                                } catch (e: Exception) {
                                    // Log any exceptions that occur
                                    Log.e(TAG,e.message.toString())
                                    // Display an unexpected error message to the user
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Log any exceptions that occur
                        Log.e(TAG,e.message.toString())
                        // Display an unexpected error message to the user
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }
            }
        }
    }

    /**
     * Initializes the faculty drop-down list.
     * This function populates the faculty drop-down list with faculties retrieved from the repository.
     */
    private fun initFacultyDropDown()
    {
        binding.apply {
            MainScope().launch(Dispatchers.IO)
            {
                try {
                    // Clear the faculty list to start fresh
                    facultyList.clear()
                    // Retrieve all faculty documents from the repository
                    val documents = FacultyRepository.getAllFacultyDocuments()
                    // Iterate through the documents and add them to the faculty list
                    for(document in documents)
                    {
                        facultyList.add(document.id)
                    }
                    // Update the UI on the main thread
                    withContext(Dispatchers.Main)
                    {
                        try {
                            // Create an ArrayAdapter for the faculty drop-down
                            facultyAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,facultyList)
                            // Set the adapter in faculty drop-down
                            actFaculty.setAdapter(facultyAdapter)
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG,e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
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
    }


    /**
     * Initializes the batch drop-down list.
     * This function populates the batch drop-down list with batches retrieved from the repository.
     */
    private fun initBatchDropDown()
    {
        binding.apply {
            MainScope().launch(Dispatchers.IO) {
                try {
                    // Clear the batch list to start fresh
                    batchList.clear()
                    AddLessonFragment.apply {
                        // Retrieve academic, semester, and class document IDs and make local variable of that
                        val academicDocumentId = "${academicYear}_$academicType"
                        val semesterDocumentId = semesterNumber
                        val classDocumentId = className


                        if(academicDocumentId!=null && semesterDocumentId!=null && classDocumentId!=null)
                        {
                            // Retrieve all batch documents from the repository
                            val documents = BatchRepository.getAllBatchDocuments(academicDocumentId, semesterDocumentId, classDocumentId)

                            // Iterate through the documents and add them to the batch list
                            for (document in documents)
                            {
                                batchList.add(document.id)
                            }
                        }
                        // Update the UI on the main thread
                        withContext(Dispatchers.Main)
                        {
                            try {
                                // Create an ArrayAdapter for the batch drop-down
                                batchAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,batchList)
                                // Set the adapter into batch drop-down
                                actBatch.setAdapter(batchAdapter)
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
            }
        }
    }


    /**
     * Initializes the room drop-down list.
     * This function populates the room drop-down list with rooms retrieved from the repository based on the selected lesson type.
     */
    private fun initRoomDropDown()
    {
        binding.apply {
            MainScope().launch(Dispatchers.IO)
            {
                try {
                    // Clear the room list to start fresh
                    roomList.clear()

                    // Retrieve room documents based on the selected lesson type LAB or CLASS
                    // because the room have two type LAB or CLASS
                    val documents = getRoomDocumentsByField("type",selectedLessonType!!)
                    // Iterate through the room documents and add them to the room list
                    for(document in documents)
                    {
                        roomList.add(document.id)
                    }
                    withContext(Dispatchers.Main)
                    {
                        try {
                            // Create an ArrayAdapter for the room drop-down list
                            roomAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,roomList)
                            // Set the adapter for room drop-down
                            actRoom.setAdapter(roomAdapter)
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG,e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
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
    }

    /**
     * Validates the fields for creating a new lesson.
     * This function ensures that all required fields are filled and valid.
     * It also displays error messages for any invalid fields.
     * @return true if all fields are valid, false otherwise.
     */
    fun validateFields(): Boolean {
        try {
            // Validate each field
            validateSubject() // Validate the subject field
            validateFaculty() // Validate the faculty field
            validateLessonType() // Validate the lesson type field
            validateRoom() // Validate the room field
            validateLessonTime() // Validate the lesson time field

            // Check return true if not error available otherwise false
            return (binding.tlSubject.error == null)
                    && (binding.tlFaculty.error == null)
                    && (binding.tlLessonType.error == null)
                    && (binding.tlRoom.error == null)
                    && (binding.tlSelectLessonTime.error == null)
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Shows a time picker dialog for selecting lesson time range.
     *
     * @param textInput The TextView where the selected time range will be displayed.
     */
    private fun showTimePicker(textInput: TextView) {
        try {
            // Create a TimeRangePickerDialog instance
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
                        try {
                            // Convert the selected start and end times to 12-hour format
                            val selectedStartTime = convertTo12HourFormat(startHour,startMinute)
                            val selectedEndTime = convertTo12HourFormat(endHour,endMinute)


                            // Validate the selected time range
                            if(validateTime(selectedStartTime, selectedEndTime)) {
                                if(selectedStartTime!=selectedEndTime)
                                {
                                    // If the start and end times are different, display the selected time range
                                    binding.tlSelectLessonTime.error = null
                                    textInput.text = "$selectedStartTime - $selectedEndTime"
                                }
                                else{
                                    // If the start and end times are the same, display an error
                                    textInput.text = ""
                                    binding.tlSelectLessonTime.error = "End time must be not be same"
                                }
                            }
                            else {
                                // If the end time is before the start time, display an error
                                textInput.text = ""
                                binding.tlSelectLessonTime.error = "End time must be after start time"
                            }
                        } catch (e: Exception) {
                            // Log any unexpected exceptions that occur
                            Log.e(TAG,e.message.toString())
                            // Display an unexpected error message to the user
                            requireContext().unexpectedErrorMessagePrint(e)
                            throw e
                        }
                    }

                }
            )
            // Set the dialog to be not cancelable
            dialog.isCancelable = false
            // Show the dialog
            dialog.show(requireActivity().supportFragmentManager, "Select Lesson Time")
        } catch (e: Exception) {
            // Log any unexpected exceptions that occur
            Log.e(TAG,e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            throw e
        }
    }

    /**
     * Validates the subject field.
     * This function checks if the subject field is filled and displays an error message if it is empty.
     * @return true if the subject field is valid (not empty), false otherwise.
     */
    fun validateSubject(): Boolean {
        try {
            var isValid = true // Flag to track the validity of the subject field
            binding.tlSubject.error = null // Clear any previous error message
            selectedSubject = binding.actSubject.text.toString() // Get the text from the subject field

            // Check if the subject field is empty or blank
            if (selectedSubject!!.isEmpty() || selectedSubject!!.isBlank()) {
                isValid = false // Set isValid to false if the field is empty
                binding.tlSubject.error = "Required" // Display "Required" error message
            }

            return isValid // Return the validity status of the subject field
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Validates the faculty field.
     * This function checks if the faculty field is filled and displays an error message if it is empty.
     * @return true if the faculty field is valid (not empty), false otherwise.
     */
    fun validateFaculty(): Boolean {
        try {
            var isValid = true // Flag to track the validity of the faculty field
            binding.tlFaculty.error = null // Clear any previous error message
            selectedFaculty = binding.actFaculty.text.toString() // Get the text from the faculty field

            // Check if the faculty field is empty or blank
            if (selectedFaculty!!.isEmpty() || selectedFaculty!!.isBlank()) {
                isValid = false // Set isValid to false if the field is empty
                binding.tlFaculty.error = "Required" // Display "Required" error message
            }

            return isValid // Return the validity status of the faculty field
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Validates the lesson type field.
     * This function checks if the lesson type field is filled and displays an error message if it is empty.
     * @return true if the lesson type field is valid (not empty), false otherwise.
     */
    fun validateLessonType(): Boolean {
        try {
            var isValid = true // Flag to track the validity of the lesson type field
            binding.tlLessonType.error = null // Clear any previous error message
            selectedLessonType = binding.actLessonType.text.toString() // Get the text from the lesson type field

            // Check if the lesson type field is empty or blank
            if (selectedLessonType!!.isEmpty() || selectedLessonType!!.isBlank()) {
                isValid = false // Set isValid to false if the field is empty
                binding.tlLessonType.error = "Required" // Display "Required" error message
            }

            return isValid // Return the validity status of the lesson type field
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Validates the room field.
     * This function checks if the room field is filled and displays an error message if it is empty.
     * @return true if the room field is valid (not empty), false otherwise.
     */
    fun validateRoom(): Boolean {
        try {
            var isValid = true // Flag to track the validity of the room field
            binding.tlRoom.error = null // Clear any previous error message
            selectedRoom = binding.actRoom.text.toString() // Get the text from the room field

            // Check if the room field is empty or blank
            if (selectedRoom!!.isEmpty() || selectedRoom!!.isBlank()) {
                isValid = false // Set isValid to false if the field is empty
                binding.tlRoom.error = "Required" // Display "Required" error message
            }

            return isValid // Return the validity status of the room field
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Validates the lesson time field.
     * This function checks if the lesson time field is filled and displays an error message if it is empty.
     * @return true if the lesson time field is valid (not empty), false otherwise.
     */
    fun validateLessonTime(): Boolean {
        try {
            var isValid = true // Flag to track the validity of the lesson time field
            binding.tlSelectLessonTime.error = null // Clear any previous error message
            selectedTime = binding.txtSelectLessonTime.text.toString() // Get the text from the lesson time field

            // Check if the lesson time field is empty, blank, or null
            if (selectedTime.isNullOrEmpty() || selectedTime.isNullOrBlank()) {
                isValid = false // Set isValid to false if the field is empty, blank, or null
                binding.tlSelectLessonTime.error = "Required" // Display "Required" error message
            }

            return isValid // Return the validity status of the lesson time field
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }


    /**
     * Validates the batch field.
     * This function checks if the batch field is filled and displays an error message if it is empty.
     * @return true if the batch field is valid (not empty), false otherwise.
     */
    fun validateBatch(): Boolean {
        try {
            var isValid = true // Flag to track the validity of the batch field
            binding.actBatch.error = null // Clear any previous error message
            selectedBatch = binding.actBatch.text.toString() // Get the text from the batch field

            // Check if the batch field is empty or blank
            if (selectedBatch.isNullOrEmpty() || selectedBatch.isNullOrBlank()) {
                isValid = false // Set isValid to false if the field is empty
                binding.tlBatch.error = "Required" // Display "Required" error message
            }

            return isValid // Return the validity status of the batch field
        } catch (e: Exception) {
            // Log any exceptions that occur during validation
            Log.e(TAG, e.message.toString())
            // Display an unexpected error message to the user
            requireContext().unexpectedErrorMessagePrint(e)
            // Propagate the exception up the call stack
            throw e
        }
    }



}