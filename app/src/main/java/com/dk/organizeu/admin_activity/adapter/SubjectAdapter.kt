package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.pojo.SubjectPojo
import com.dk.organizeu.admin_activity.listener.OnSubjectItemClickListener

class SubjectAdapter(private val subjectPojoList: ArrayList<SubjectPojo>, private val listener: OnSubjectItemClickListener) :
    RecyclerView.Adapter<SubjectAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = subjectPojoList[position]
        holder.subjectNameTxt.text = "Name: ${currentItem.name}"
        holder.subjectCodeTxt.text = "Code: ${currentItem.code}"
        holder.subjectTypeTxt.text = "Type: ${currentItem.type}"
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return subjectPojoList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectNameTxt: TextView = itemView.findViewById(R.id.txtSubjectName)
        val subjectCodeTxt: TextView = itemView.findViewById(R.id.txtSubjectCode)
        val subjectTypeTxt: TextView = itemView.findViewById(R.id.txtSubjectType)
    }

}