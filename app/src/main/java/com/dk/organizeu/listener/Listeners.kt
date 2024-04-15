package com.dk.organizeu.listener

interface AddDocumentListener {
    fun onAdded(documentId: String,documentData: HashMap<String,String>)
}


interface OnItemClickListener {
    fun onClick(position: Int)
}