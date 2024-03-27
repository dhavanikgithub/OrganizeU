package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R

class AddSemAdapter(private val academicSemList: ArrayList<String>) :
    RecyclerView.Adapter<AddSemAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_sem, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = academicSemList[position]
        holder.textViewSem.text = "Semester: ${currentItem}"
    }

    override fun getItemCount(): Int {
        return academicSemList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textViewSem: TextView = itemView.findViewById(R.id.textView)
        init {
            itemView.setOnClickListener {

            }
        }

    }

}