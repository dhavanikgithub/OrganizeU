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
        binding = DataBindingUtil.bind(view)!!
        return AcademicViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
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

    fun itemRemove(position: Int)
    {
        val itemChangedCount = academicList.size - position
        notifyItemRemoved(position)
        academicList.removeAt(position)
        notifyItemRangeChanged(position, itemChangedCount)
    }

    fun itemInsert(position: Int,academicPojo: AcademicPojo)
    {
        academicList.add(academicPojo)
        notifyItemInserted(position)
    }

    fun itemModify(position: Int,academicPojo: AcademicPojo)
    {
        academicList.removeAt(position)
        academicList.add(position,academicPojo)
        notifyItemChanged(position)
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}