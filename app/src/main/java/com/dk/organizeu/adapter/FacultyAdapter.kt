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
import com.dk.organizeu.pojo.FacultyPojo

class FacultyAdapter(private val facultyPojos: ArrayList<FacultyPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<FacultyAdapter.AcademicViewHolder>() {

        private lateinit var binding: ItemFacultyBinding

    companion object{
        const val TAG = "OrganizeU-FacultyAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty, parent, false)
        return AcademicViewHolder(view)
    }


    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            binding = DataBindingUtil.bind(holder.itemView)!!
            val currentItem = facultyPojos[holder.adapterPosition]
            binding.facultyPojo = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return facultyPojos.size
    }

    fun itemDelete(position: Int)
    {
        facultyPojos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position,itemCount-position)
    }

    fun itemInsert(facultyPojo: FacultyPojo)
    {
        facultyPojos.add(facultyPojo)
        notifyItemInserted(itemCount)
    }

    fun itemModify(facultyPojo: FacultyPojo)
    {
        val index = facultyPojos.indexOfFirst {
            it.id==facultyPojo.id
        }
        if(index<0)
        {
            return
        }
        facultyPojos[index] = facultyPojo
        notifyItemChanged(index)
    }

    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}