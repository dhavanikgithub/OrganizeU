package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemFacultyBinding
import com.dk.organizeu.listener.OnItemClickListener

class FacultyAdapter(private val facultyList: ArrayList<String>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<FacultyAdapter.AcademicViewHolder>() {

        private lateinit var binding: ItemFacultyBinding

    companion object{
        const val TAG = "OrganizeU-FacultyAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty, parent, false)
        binding = DataBindingUtil.bind(view)!!
        return AcademicViewHolder(binding.root)
    }


    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            val currentItem = facultyList[holder.adapterPosition]
            binding.facultyName = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return facultyList.size
    }

    fun itemDelete(position: Int)
    {
        val itemChangedCount = facultyList.size - position
        notifyItemRemoved(position)
        facultyList.removeAt(position)
        notifyItemRangeChanged(position, itemChangedCount)
    }

    fun itemInsert(facultyName:String)
    {
        facultyList.add(facultyName)
        notifyItemInserted(itemCount)
    }

    fun itemModify(oldFacultyName:String, facultyName: String)
    {
        val index = facultyList.indexOfFirst {
            it==oldFacultyName
        }
        if(index<0)
        {
            return
        }
        facultyList[index] = facultyName
        notifyItemRangeChanged(index, facultyList.size)
    }


    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}