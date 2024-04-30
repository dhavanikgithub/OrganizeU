package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemRoomBinding
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.RoomPojo


class RoomAdapter(private val roomPojoList: ArrayList<RoomPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<RoomAdapter.AcademicViewHolder>() {

        private lateinit var binding: ItemRoomBinding

    companion object{
        const val TAG = "OrganizeU-RoomAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        binding = DataBindingUtil.bind(view)!!
        return AcademicViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = roomPojoList[holder.adapterPosition]
            binding.roomPojo = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return roomPojoList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}