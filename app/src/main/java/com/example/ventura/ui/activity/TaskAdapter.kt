package com.example.ventura.ui.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.ventura.R
import com.example.ventura.database.Task

class TaskAdapter(private val context: Context, private val tasks: List<Task>) : BaseAdapter() {

    override fun getCount(): Int = tasks.size

    override fun getItem(position: Int): Any = tasks[position]

    override fun getItemId(position: Int): Long = tasks[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_list_item, parent, false)
        val task = tasks[position]

        val titleTextView = view.findViewById<TextView>(R.id.taskTitleTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.taskDescriptionTextView)

        titleTextView.text = task.title
        descriptionTextView.text = task.description

        return view
    }
}
