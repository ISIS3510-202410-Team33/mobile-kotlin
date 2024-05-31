package com.example.ventura.ui.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R
import com.example.ventura.model.Task
import com.example.ventura.viewmodel.TasksViewModel

class TaskAdapter(
    private val context: Context,
    private var tasks: List<Task>,
    private val viewModel: TasksViewModel // ViewModel reference to handle task updates
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.taskTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.taskDescriptionTextView)
        private val completedCheckBox: CheckBox = itemView.findViewById(R.id.taskCompletedCheckBox)

        fun bind(task: Task) {
            titleTextView.text = task.title
            descriptionTextView.text = task.description
            completedCheckBox.isChecked = task.completed

            if (task.completed) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                descriptionTextView.paintFlags = descriptionTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                titleTextView.paintFlags = titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                descriptionTextView.paintFlags = descriptionTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                task.completed = isChecked
                viewModel.updateTask(task)
            }
        }
    }
}
