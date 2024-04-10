package com.dk.organizeu.admin_activity.fragments.academic.add_academic.add_class

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.dk.organizeu.adapter.ClassAdapter

class AddClassViewModel : ViewModel() {
    var academicTypeItemList:ArrayList<String> = ArrayList()
    var academicYearItemList:ArrayList<String> = ArrayList()
    var academicSemItemList:ArrayList<Int> = ArrayList()
    var academicClassList:ArrayList<String> = ArrayList()
    lateinit var academicTypeItemAdapter: ArrayAdapter<String>
    lateinit var academicYearItemAdapter: ArrayAdapter<String>
    lateinit var academicSemItemAdapter: ArrayAdapter<Int>
    lateinit var academicClassAdapter: ClassAdapter
    var academicYearSelectedItem:String?=null
    var academicTypeSelectedItem:String?=null
    var academicSemSelectedItem:String?=null
}