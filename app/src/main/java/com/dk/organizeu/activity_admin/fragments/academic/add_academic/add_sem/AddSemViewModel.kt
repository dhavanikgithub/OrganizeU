package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_sem

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.SemesterAdapter
import com.dk.organizeu.pojo.SemesterPojo

class AddSemViewModel : ViewModel() {
    var academicTypeItemList:ArrayList<String> = ArrayList()
    var academicYearItemList:ArrayList<String> = ArrayList()
    var academicSemItemList:ArrayList<Int> = ArrayList()
    var academicSemList:ArrayList<SemesterPojo> = ArrayList()
    lateinit var academicTypeItemAdapter: ArrayAdapter<String>
    lateinit var academicYearItemAdapter: ArrayAdapter<String>
    lateinit var academicSemItemAdapter: ArrayAdapter<Int>
    lateinit var academicSemesterAdapter: SemesterAdapter
    var academicYearSelectedItem:String?=null
    var academicTypeSelectedItem:String?=null
    var academicSemSelectedItem:String?=null
}