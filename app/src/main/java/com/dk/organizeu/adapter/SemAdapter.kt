package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R

class SemAdapter(private val academicSemList: ArrayList<String>) :
    RecyclerView.Adapter<SemAdapter.AcademicViewHolder>() {

    companion object{
        const val TAG = "OrganizeU-SemAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_sem, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicSemList[position]
            holder.semNumberTxt.text = "Semester: ${currentItem}"
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return academicSemList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val semNumberTxt: TextView = itemView.findViewById(R.id.txtSemesterNumber)
    }

}