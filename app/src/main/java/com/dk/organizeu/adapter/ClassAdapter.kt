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
import com.dk.organizeu.pojo.ClassPojo

class ClassAdapter(private val classPojos: ArrayList<ClassPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<ClassAdapter.AcademicViewHolder>() {
        private lateinit var binding: ItemAddClassBinding

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
            binding = DataBindingUtil.bind(holder.itemView)!!
            val currentItem = classPojos[holder.adapterPosition]
            binding.classPojo = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return classPojos.size
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}