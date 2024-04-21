package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.RoomPojo


class RoomAdapter(private val roomPojoList: ArrayList<RoomPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<RoomAdapter.AcademicViewHolder>() {

    companion object{
        const val TAG = "OrganizeU-RoomAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = roomPojoList[position]
            holder.roomNameTxt.text = "Name: ${currentItem.name}"
            holder.roomLocationTxt.text = "Location: ${currentItem.location}"
            holder.roomTypeTxt.text = "Type: ${currentItem.type}"
            holder.itemView.setOnClickListener {
                listener.onClick(position)
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return roomPojoList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNameTxt: TextView = itemView.findViewById(R.id.txtRoomName)
        val roomLocationTxt: TextView = itemView.findViewById(R.id.txtRoomLocation)
        val roomTypeTxt: TextView = itemView.findViewById(R.id.txtRoomType)
    }

}