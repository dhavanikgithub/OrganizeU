package com.dk.organizeu.listener

interface AddDocumentListener {
    fun onAdded(documentId: String,documentData: HashMap<String,String>)
}

interface EditDocumentListener {
    fun onEdited(oldDocumentId:String,newDocumentId: String,documentData: HashMap<String,String>)
}


interface OnItemClickListener {
    fun onClick(position: Int)

    fun onDeleteClick(position: Int)

    fun onEditClick(position: Int)
}
