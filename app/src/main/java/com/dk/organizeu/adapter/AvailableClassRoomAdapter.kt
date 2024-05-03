package com.dk.organizeu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemAvailableClassRoomLayoutBinding
import com.dk.organizeu.pojo.AvailableClassRoomPojo

class AvailableClassRoomAdapter(val availableClassRoomPojos: ArrayList<AvailableClassRoomPojo>): RecyclerView.Adapter<AvailableClassRoomAdapter.AvailableClassRoomViewHolder>() {

    lateinit var binding: ItemAvailableClassRoomLayoutBinding

    class AvailableClassRoomViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AvailableClassRoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_available_class_room_layout,parent,false)
        return AvailableClassRoomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return availableClassRoomPojos.size
    }

    override fun onBindViewHolder(holder: AvailableClassRoomViewHolder, position: Int) {
        binding = DataBindingUtil.bind(holder.itemView)!!
        val currentItem = availableClassRoomPojos[holder.adapterPosition]
        binding.availableClassRoomPojo = currentItem
    }
}