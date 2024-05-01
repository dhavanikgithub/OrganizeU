package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemAddSemBinding
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.SemesterPojo

class SemesterAdapter(private val semesterPojos: ArrayList<SemesterPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SemesterAdapter.AcademicViewHolder>() {

    companion object{
        const val TAG = "OrganizeU-SemAdapter"
    }
    lateinit var binding: ItemAddSemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_sem, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            binding = DataBindingUtil.bind(holder.itemView)!!
            val currentItem = semesterPojos[holder.adapterPosition]
            binding.semesterPojo = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return semesterPojos.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}