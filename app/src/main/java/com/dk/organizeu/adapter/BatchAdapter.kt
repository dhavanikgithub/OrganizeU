package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.listener.OnItemClickListener

class BatchAdapter(private val academicBatchList: ArrayList<String>,private val listener: OnItemClickListener) :
    RecyclerView.Adapter<BatchAdapter.AcademicViewHolder>() {

    companion object{
        const val TAG = "OrganizeU-BatchAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_batch, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicBatchList[position]
            holder.batchNameTxt.text = "Batch: ${currentItem}"
            holder.itemView.setOnClickListener {
                listener.onClick(position)
            }
            holder.btnDelete.setOnClickListener {
                listener.onDeleteClick(position)
            }
            holder.btnEdit.setOnClickListener {
                listener.onEditClick(position)
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {

        return academicBatchList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val batchNameTxt: TextView = itemView.findViewById(R.id.txtBatchName)
        val btnEdit: LinearLayout = itemView.findViewById(R.id.btnEdit)
        val btnDelete: LinearLayout = itemView.findViewById(R.id.btnDelete)
    }

}