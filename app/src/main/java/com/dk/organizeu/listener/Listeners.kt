package com.dk.organizeu.listener

import com.dk.organizeu.pojo.*

interface AddDocumentListener {
    fun onAdded(documentId: String,documentData: HashMap<String,String>)
}

interface DrawerLocker {
    fun setDrawerEnabled(enabled: Boolean)
}
interface FacultyDocumentListener {
    fun onAdded(facultyPojo: FacultyPojo)
    fun onEdited(facultyPojo: FacultyPojo)
}

interface AcademicDocumentListener {
    fun onAdded(academicPojo: AcademicPojo)
}

interface RoomDocumentListener {
    fun onAdded(roomPojo: RoomPojo)
    fun onEdited(roomPojo: RoomPojo,position: Int)
}

interface SubjectDocumentListener {
    fun onAdded(subjectPojo: SubjectPojo)
    fun onEdited(subjectPojo: SubjectPojo,position: Int)
}

interface ClassDocumentListener {
    fun onEdited(classPojo: ClassPojo, position: Int)
}

interface BatchDocumentListener {
    fun onEdited(batchPojo: BatchPojo, position: Int)
}
interface EditDocumentListener {
    fun onEdited(oldDocumentId:String,newDocumentId: String,documentData: HashMap<String,String>)
}


interface OnItemClickListener {
    fun onClick(position: Int)

    fun onDeleteClick(position: Int)

    fun onEditClick(position: Int)
}
