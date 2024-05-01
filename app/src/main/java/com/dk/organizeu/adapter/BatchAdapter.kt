package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemAddBatchBinding
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.BatchPojo

class BatchAdapter(private val batchPojos: ArrayList<BatchPojo>,private val listener: OnItemClickListener) :
    RecyclerView.Adapter<BatchAdapter.AcademicViewHolder>() {

        private lateinit var binding: ItemAddBatchBinding

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
            binding = DataBindingUtil.bind(holder.itemView)!!
            val currentItem = batchPojos[holder.adapterPosition]
            binding.batchPojo = currentItem
            binding.position = holder.adapterPosition
            binding.listener = listener
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return batchPojos.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}