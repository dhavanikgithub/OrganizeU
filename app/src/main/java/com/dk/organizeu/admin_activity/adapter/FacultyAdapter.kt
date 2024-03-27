package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R

class FacultyAdapter(private val facultyList: ArrayList<String>) :
    RecyclerView.Adapter<FacultyAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = facultyList[position]
        holder.facultyNameTxt.text = currentItem
    }

    override fun getItemCount(): Int {
        return facultyList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val facultyNameTxt: TextView = itemView.findViewById(R.id.facultyNameTxt)
    }

}