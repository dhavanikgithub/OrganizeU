package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.pojo.AcademicPojo
import com.dk.organizeu.listener.OnItemClickListener

class AcademicAdapter(private val academicList: List<AcademicPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AcademicAdapter.AcademicViewHolder>() {
    companion object{
        const val TAG = "OrganizeU-AcademicAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_academic, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicList[position]
            holder.academicYearTxt.text = "Academic: ${currentItem.academic}"
            holder.academicTypeTxt.text = "Type: ${currentItem.sem}"

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
        return academicList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val academicYearTxt: TextView = itemView.findViewById(R.id.txtAcademicYear)
        val academicTypeTxt: TextView = itemView.findViewById(R.id.txtAcademicType)
        val btnEdit: LinearLayout = itemView.findViewById(R.id.btnEdit)
        val btnDelete: LinearLayout = itemView.findViewById(R.id.btnDelete)

    }

}