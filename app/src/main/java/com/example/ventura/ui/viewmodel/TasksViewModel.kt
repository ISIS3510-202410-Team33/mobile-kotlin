package com.example.ventura.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ventura.database.DatabaseHelper
import com.example.ventura.model.Task
import java.time.LocalDate

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks

    private val _selectedDate = MutableLiveData<LocalDate?>()
    val selectedDate: LiveData<LocalDate?> get() = _selectedDate

    fun setSelectedDate(date: LocalDate?) {
        _selectedDate.value = date
        loadTasksForDate(date)
    }

    fun loadTasksForDate(date: LocalDate?) {
        _tasks.value = dbHelper.getTasksForDate(date)
    }

    fun addTask(date: LocalDate, title: String, description: String) {
        dbHelper.insertTask(date, title, description)
        loadTasksForDate(date)
    }
}
