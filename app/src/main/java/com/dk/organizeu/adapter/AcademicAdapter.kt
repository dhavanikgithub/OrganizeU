package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemAcademicBinding
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.AcademicPojo

class AcademicAdapter(private val academicList: ArrayList<AcademicPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AcademicAdapter.AcademicViewHolder>() {
    private lateinit var binding: ItemAcademicBinding
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
            binding = DataBindingUtil.bind(holder.itemView)!!
            val currentItem = academicList[holder.adapterPosition]
            binding.academicPojo = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return academicList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}