package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R

class AddClassAdapter(private val academicClassList: ArrayList<String>) :
    RecyclerView.Adapter<AddClassAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_class, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = academicClassList[position]
        holder.textViewSem.text = "Class: ${currentItem}"
    }

    override fun getItemCount(): Int {

        return academicClassList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textViewSem: TextView = itemView.findViewById(R.id.textView)

    }

}