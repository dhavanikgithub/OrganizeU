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

class BatchAdapter(private val academicBatchList: ArrayList<String>,private val listener: OnItemClickListener) :
    RecyclerView.Adapter<BatchAdapter.AcademicViewHolder>() {

        private lateinit var binding: ItemAddBatchBinding

    companion object{
        const val TAG = "OrganizeU-BatchAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_batch, parent, false)
        binding = DataBindingUtil.bind(view)!!
        return AcademicViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicBatchList[position]
            binding.batch = currentItem
            binding.position = position
            binding.listener = listener
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {

        return academicBatchList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}