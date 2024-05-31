package com.example.ventura.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import com.example.ventura.viewmodel.TasksViewModel
import java.time.LocalDate

class NewTaskActivity : AppCompatActivity() {

    private val tasksViewModel: TasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        val taskTitleInput = findViewById<EditText>(R.id.taskTitleInput)
        val taskDescriptionInput = findViewById<EditText>(R.id.taskDescriptionInput)
        val saveTaskButton = findViewById<Button>(R.id.saveTaskButton)

        val selectedDate = LocalDate.parse(intent.getStringExtra("selectedDate"))

        saveTaskButton.setOnClickListener {
            val title = taskTitleInput.text.toString()
            val description = taskDescriptionInput.text.toString()
            tasksViewModel.addTask(selectedDate, title, description)
            finish()
        }
    }
}
