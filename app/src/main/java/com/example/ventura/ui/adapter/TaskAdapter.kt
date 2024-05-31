package com.example.ventura.ui.adapter

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
    private var tasks: MutableList<Task>,
    private val tasksViewModel: TasksViewModel
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = task.completed
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            task.completed = isChecked
            tasksViewModel.updateTask(task)
            setStrikeThrough(holder.title, isChecked)
            setStrikeThrough(holder.description, isChecked)
        }

        setStrikeThrough(holder.title, task.completed)
        setStrikeThrough(holder.description, task.completed)

        holder.title.text = task.title
        holder.description.text = task.description
    }

    private fun setStrikeThrough(textView: TextView, strikeThrough: Boolean) {
        if (strikeThrough) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.taskCompletedCheckBox)
        val title: TextView = view.findViewById(R.id.taskTitleTextView)
        val description: TextView = view.findViewById(R.id.taskDescriptionTextView)
    }
}