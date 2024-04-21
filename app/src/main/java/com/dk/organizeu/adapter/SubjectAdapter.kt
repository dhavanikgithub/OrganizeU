package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.SubjectPojo


class SubjectAdapter(private val subjectPojoList: ArrayList<SubjectPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SubjectAdapter.AcademicViewHolder>() {


    companion object{
        const val TAG = "OrganizeU-SubjectAdapter"
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = subjectPojoList[position]
            holder.subjectNameTxt.text = "Name: ${currentItem.name}"
            holder.subjectCodeTxt.text = "Code: ${currentItem.code}"
            holder.subjectTypeTxt.text = "Type: ${currentItem.type}"
            holder.itemView.setOnClickListener {
                listener.onClick(position)
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
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