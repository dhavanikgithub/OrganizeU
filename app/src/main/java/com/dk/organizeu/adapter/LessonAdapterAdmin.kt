package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ItemLessonAdminBinding
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.pojo.LessonPojo


class LessonAdapterAdmin(private val lessonPojos: ArrayList<LessonPojo>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_ITEM = 1
    private var isItemViewHolder = false
    companion object{
        const val TAG = "OrganizeU-LessonAdapter"
    }

    private var binding: ItemLessonAdminBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        try {
            return when (viewType) {
                VIEW_TYPE_EMPTY -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.empty_view_layout, parent, false)
                    isItemViewHolder = false
                    EmptyViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_admin, parent, false)
                    isItemViewHolder = true
                    ItemViewHolder(view)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            throw e
        }
        /*val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timetable, parent, false)
        return ViewHolder(view)*/
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            if(!isItemViewHolder)
            {
                return
            }
            binding = DataBindingUtil.bind(holder.itemView)!!
            val item = lessonPojos[holder.adapterPosition]

            if(binding!=null)
            {
                binding!!.timetablePojo = item
                binding!!.position = holder.adapterPosition
                binding!!.listener = listener
            }

        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return if (lessonPojos.isEmpty()) {
            1
        } else {
            lessonPojos.size
        }
    }

    /*override fun getItemCount(): Int = timetableItems.size*/
    override fun getItemViewType(position: Int): Int {
        return if (lessonPojos.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_ITEM
        }
    }
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
