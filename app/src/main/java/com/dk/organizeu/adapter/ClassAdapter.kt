package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R

class ClassAdapter(private val academicClassList: ArrayList<String>) :
    RecyclerView.Adapter<ClassAdapter.AcademicViewHolder>() {

    companion object{
        const val TAG = "OrganizeU-ClassAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_class, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicClassList[position]
            holder.classNameTxt.text = "Class: ${currentItem}"
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return academicClassList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val classNameTxt: TextView = itemView.findViewById(R.id.txtClassName)

    }

}