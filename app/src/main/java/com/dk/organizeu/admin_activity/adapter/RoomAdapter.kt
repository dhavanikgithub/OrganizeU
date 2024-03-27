package com.dk.organizeu.admin_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.data_class.Room

class RoomAdapter(private val roomList: ArrayList<Room>) :
    RecyclerView.Adapter<RoomAdapter.AcademicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        val currentItem = roomList[position]
        holder.roomNameTxt.text = "Name: ${currentItem.name}"
        holder.roomLocationTxt.text = "Location: ${currentItem.location}"
        holder.roomTypeTxt.text = "Type: ${currentItem.type}"
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNameTxt: TextView = itemView.findViewById(R.id.roomNameTxt)
        val roomLocationTxt: TextView = itemView.findViewById(R.id.roomLocationTxt)
        val roomTypeTxt: TextView = itemView.findViewById(R.id.roomTypeTxt)
    }

}