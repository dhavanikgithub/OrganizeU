package com.dk.organizeu.activity_admin.fragments.timetable

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel

class TimetableViewModel : ViewModel() {
    var academicYearAdapter: ArrayAdapter<String>? = null
    val academicYearList: ArrayList<String> = ArrayList()
    var academicTypeAdapter: ArrayAdapter<String>? = null
    val academicTypeList: ArrayList<String> = ArrayList()
    var semesterAdapter: ArrayAdapter<String>? = null
    val semesterList: ArrayList<String> = ArrayList()
    var classAdapter: ArrayAdapter<String>? = null
    val classList: ArrayList<String> = ArrayList()


    var selectedAcademicYearItem:String? = null
    var selectedAcademicTypeItem:String? = null
    var selectedSemesterItem:String? = null
    var selectedClassItem:String? = null

}