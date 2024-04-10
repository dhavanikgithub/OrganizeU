package com.dk.organizeu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R

class BatchAdapter(private val academicBatchList: ArrayList<String>) :
    RecyclerView.Adapter<BatchAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_batch, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = academicBatchList[position]
        holder.batchNameTxt.text = "Batch: ${currentItem}"
    }

    override fun getItemCount(): Int {

        return academicBatchList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val batchNameTxt: TextView = itemView.findViewById(R.id.txtBatchName)

    }

}