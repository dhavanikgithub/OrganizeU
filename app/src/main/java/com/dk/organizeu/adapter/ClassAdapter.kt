package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemAddClassBinding
import com.dk.organizeu.listener.OnItemClickListener

class ClassAdapter(private val academicClassList: ArrayList<String>,private val listener: OnItemClickListener) :
    RecyclerView.Adapter<ClassAdapter.AcademicViewHolder>() {
        private lateinit var binding: ItemAddClassBinding

    companion object{
        const val TAG = "OrganizeU-ClassAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_class, parent, false)
        binding = DataBindingUtil.bind(view)!!
        return AcademicViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicClassList[holder.adapterPosition]
            binding.className = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return academicClassList.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}