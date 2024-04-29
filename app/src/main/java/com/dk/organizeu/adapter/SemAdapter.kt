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

class SemAdapter(private val academicSemList: ArrayList<String>,private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SemAdapter.AcademicViewHolder>() {

    companion object{
        const val TAG = "OrganizeU-SemAdapter"
    }
    lateinit var binding: ItemAddSemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_sem, parent, false)
        binding = DataBindingUtil.bind(view)!!
        return AcademicViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = academicSemList[position]
            binding.semesterNumber = currentItem
            binding.listener = listener
            binding.position = position
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return academicSemList.size
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}