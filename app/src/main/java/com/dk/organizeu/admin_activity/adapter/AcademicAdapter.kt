package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.data_class.AcademicItem
import com.dk.organizeu.admin_activity.listener.OnAcademicItemClickListener

class AcademicAdapter(private val academicList: List<AcademicItem>,private val listener: OnAcademicItemClickListener) :
    RecyclerView.Adapter<AcademicAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_academic, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = academicList[position]
        holder.academicYearTxt.text = "Academic: ${currentItem.academic}"
        holder.academicTypeTxt.text = "Type: ${currentItem.sem}"

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return academicList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val academicYearTxt: TextView = itemView.findViewById(R.id.academicYearTxt)
        val academicTypeTxt: TextView = itemView.findViewById(R.id.academicTypeTxt)

    }

}