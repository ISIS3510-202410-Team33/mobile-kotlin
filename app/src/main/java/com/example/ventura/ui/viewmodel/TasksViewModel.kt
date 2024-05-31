package com.example.ventura.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ventura.data.TaskDatabase
import com.example.ventura.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = TaskDatabase.getDatabase(application).taskDao()

    private val _tasks = MutableLiveData<List<Task>?>()
    val tasks: MutableLiveData<List<Task>?> = _tasks

    private val _selectedDate = MutableLiveData<LocalDate>()
    val selectedDate: LiveData<LocalDate> = _selectedDate

    fun loadTasksForDate(date: LocalDate?) {
        viewModelScope.launch {
            date?.let {
                _tasks.value = withContext(Dispatchers.IO) {
                    taskDao.getTasksForDate(it)
                }
            } ?: run {
                _tasks.value = emptyList()
            }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addTask(date: LocalDate, title: String, description: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val task = Task(date = date, title = title, description = description)
                taskDao.insert(task)
            }
            loadTasksForDate(date)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                taskDao.update(task)
            }
            loadTasksForDate(task.date)
        }
    }
}
