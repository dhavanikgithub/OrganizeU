package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemSubjectBinding
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.SubjectPojo


class SubjectAdapter(private val subjectPojoList: ArrayList<SubjectPojo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SubjectAdapter.AcademicViewHolder>() {


    companion object{
        const val TAG = "OrganizeU-SubjectAdapter"
    }
    private lateinit var binding:ItemSubjectBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return AcademicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcademicViewHolder, position: Int) {
        try {
            binding = DataBindingUtil.bind(holder.itemView)!!
            val currentItem = subjectPojoList[holder.adapterPosition]
            binding.subjectPojo = currentItem
            binding.listener = listener
            binding.position = holder.adapterPosition
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return subjectPojoList.size
    }



    class AcademicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}