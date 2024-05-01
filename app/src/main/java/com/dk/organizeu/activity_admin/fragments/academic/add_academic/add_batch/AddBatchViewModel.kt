package com.dk.organizeu.activity_admin.fragments.academic.add_academic.add_batch

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.BatchAdapter
import com.dk.organizeu.pojo.BatchPojo

class AddBatchViewModel : ViewModel() {
    var academicTypeItemList:ArrayList<String> = ArrayList()
    var academicYearItemList:ArrayList<String> = ArrayList()
    var academicSemItemList:ArrayList<Int> = ArrayList()
    var academicClassItemList:ArrayList<String> = ArrayList()
    var academicBatchList:ArrayList<BatchPojo> = ArrayList()
    lateinit var academicTypeItemAdapter: ArrayAdapter<String>
    lateinit var academicYearItemAdapter: ArrayAdapter<String>
    lateinit var academicSemItemAdapter: ArrayAdapter<Int>
    lateinit var academicClassItemAdapter: ArrayAdapter<String>
    lateinit var academicBatchAdapter: BatchAdapter
    var academicYearSelectedItem:String?=null
    var academicTypeSelectedItem:String?=null
    var academicSemSelectedItem:String?=null
    var academicClassSelectedItem:String?=null
}