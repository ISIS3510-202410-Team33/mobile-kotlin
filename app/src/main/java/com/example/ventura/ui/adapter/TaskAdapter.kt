package com.example.ventura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.ventura.R
import com.example.ventura.model.Task

class TaskAdapter(private val context: Context, private var tasks: List<Task>) : BaseAdapter() {

    fun updateTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return tasks.size
    }

    override fun getItem(position: Int): Any {
        return tasks[position]
    }

    override fun getItemId(position: Int): Long {
        return tasks[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
        } else {
            view = convertView
        }

        val task = tasks[position]
        val taskTitle = view.findViewById<TextView>(R.id.taskTitle)
        val taskDescription = view.findViewById<TextView>(R.id.taskDescription)

        taskTitle.text = task.title
        taskDescription.text = task.description

        return view
    }
}
