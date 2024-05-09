package com.dk.organizeu.activity_student.fragments.register

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel

class StudentRegisterViewModel : ViewModel() {
    var academicYearAdapter: ArrayAdapter<String>? = null
    val academicYearList: ArrayList<String> = ArrayList()

    var semesterAdapter: ArrayAdapter<String>? = null
    val semesterList: ArrayList<String> = ArrayList()

    var classAdapter: ArrayAdapter<String>? = null
    val classList: ArrayList<String> = ArrayList()

    var batchAdapter: ArrayAdapter<String>? = null
    val batchList: ArrayList<String> = ArrayList()

    var selectedAcademicYear:String? = null
    var selectedAcademicType:String? = null
    var selectedClass:String? = null
    var selectedSemester:String?=null
    var selectedBatch:String? = null


}