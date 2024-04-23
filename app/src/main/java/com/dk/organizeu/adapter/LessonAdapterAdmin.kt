package com.dk.organizeu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dk.organizeu.R
import com.dk.organizeu.pojo.TimetablePojo
import com.dk.organizeu.utils.UtilFunction.Companion.convert24HourTo12Hour


class LessonAdapterAdmin(private val timetablePojos: ArrayList<TimetablePojo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_ITEM = 1
    companion object{
        const val TAG = "OrganizeU-LessonAdapter"
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        try {
            return when (viewType) {
                VIEW_TYPE_EMPTY -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.empty_view_layout, parent, false)
                    EmptyViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_admin, parent, false)
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
            /*val item = timetableItems[position]
            holder.bind(item)*/
            if (holder is ItemViewHolder) {
                holder.bind(timetablePojos[position])
            }
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
        }
    }
    override fun getItemCount(): Int {
        return if (timetablePojos.isEmpty()) {
            1
        } else {
            timetablePojos.size
        }
    }

    /*override fun getItemCount(): Int = timetableItems.size*/
    override fun getItemViewType(position: Int): Int {
        return if (timetablePojos.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_ITEM
        }
    }
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TimetablePojo) {
            try {
                itemView.findViewById<TextView>(R.id.txtLessonClassName).text = "${item.className} ${item.location}"
                itemView.findViewById<TextView>(R.id.txtLessonSubjectName).text = item.subjectName
                itemView.findViewById<TextView>(R.id.txtStartTime).text = item.startTime.convert24HourTo12Hour()
                itemView.findViewById<TextView>(R.id.txtEndTime).text = item.endTime.convert24HourTo12Hour()
                itemView.findViewById<TextView>(R.id.txtLessonDuration).text = item.duration
                itemView.findViewById<TextView>(R.id.txtLessonType).text = item.type
                itemView.findViewById<TextView>(R.id.txtLessonFacultyName).text = item.facultyName
                itemView.findViewById<TextView>(R.id.txtLessonNumber).text = "Lesson: ${item.lessonNumber}"
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
            }
        }
    }
    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
