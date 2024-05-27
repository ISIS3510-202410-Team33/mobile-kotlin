package com.example.ventura.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R
import com.example.ventura.repository.Course

class CoursesAdapter(
    private val courses: MutableList<Course>,
    private val onCourseRemoved: (Course) -> Unit
) : RecyclerView.Adapter<CoursesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.courseName)
        val courseProfessor: TextView = itemView.findViewById(R.id.courseProfessor)
        val courseRoom: TextView = itemView.findViewById(R.id.courseRoom)
        val courseSchedule: TextView = itemView.findViewById(R.id.courseSchedule)
        val closeIcon: ImageView = itemView.findViewById(R.id.closeIcon)

        init {
            closeIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val course = courses[position]
                    courses.removeAt(position)
                    notifyItemRemoved(position)
                    onCourseRemoved(course)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.courseName.text = course.courseName
        holder.courseProfessor.text = "Professor: ${course.professor}"
        holder.courseRoom.text = "Room: ${course.room}"
        holder.courseSchedule.text = "Schedule: ${course.schedule}"
    }

    override fun getItemCount(): Int {
        return courses.size
    }

}
