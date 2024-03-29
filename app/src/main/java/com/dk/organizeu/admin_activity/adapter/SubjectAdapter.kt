package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.data_class.Subject
import com.dk.organizeu.admin_activity.listener.OnSubjectItemClickListener

class SubjectAdapter(private val subjectList: ArrayList<Subject>,private val listener: OnSubjectItemClickListener) :
    RecyclerView.Adapter<SubjectAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = subjectList[position]
        holder.subjectNameTxt.text = "Name: ${currentItem.name}"
        holder.subjectCodeTxt.text = "Code: ${currentItem.code}"
        holder.subjectTypeTxt.text = "Type: ${currentItem.type}"
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return subjectList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectNameTxt: TextView = itemView.findViewById(R.id.subjectNameTxt)
        val subjectCodeTxt: TextView = itemView.findViewById(R.id.subjectCodeTxt)
        val subjectTypeTxt: TextView = itemView.findViewById(R.id.subjectTypeTxt)
    }

}