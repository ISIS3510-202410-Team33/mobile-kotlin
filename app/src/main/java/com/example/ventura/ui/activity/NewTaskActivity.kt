package com.example.ventura.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import com.example.ventura.database.DatabaseHelper
import java.time.LocalDate

class NewTaskActivity : AppCompatActivity() {

    private lateinit var taskTitleEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var selectedDate: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        taskTitleEditText = findViewById(R.id.taskTitleEditText)
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText)
        saveButton = findViewById(R.id.saveButton)

        // Retrieve the selected date from the intent
        selectedDate = LocalDate.parse(intent.getStringExtra("selectedDate"))

        saveButton.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = taskTitleEditText.text.toString()
        val description = taskDescriptionEditText.text.toString()

        if (title.isNotEmpty() && description.isNotEmpty()) {
            val dbHelper = DatabaseHelper(this)
            dbHelper.insertTask(selectedDate, title, description)
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
