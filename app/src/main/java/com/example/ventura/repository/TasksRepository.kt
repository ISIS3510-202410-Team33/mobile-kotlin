package com.example.ventura.repository

import android.util.LruCache
import com.example.ventura.data.TaskDao
import com.example.ventura.model.Task
import java.time.LocalDate

class TasksRepository(private val taskDao: TaskDao) {

    private val cacheSize = 50 // Define the size of the cache

    // Create an LRU cache with the specified size and eviction policy
    private val cache: LruCache<LocalDate, List<Task>> = object : LruCache<LocalDate, List<Task>>(cacheSize) {
        override fun sizeOf(key: LocalDate, value: List<Task>): Int {
            // Return the size of the list of tasks as the size of the entry
            return value.size
        }
    }

    suspend fun getTasksForDate(date: LocalDate): List<Task> {
        // Check if the data is available in the cache
        val cachedData = cache.get(date)
        if (cachedData != null) {
            return cachedData
        }

        // If not available in cache, fetch from the database
        val tasks = taskDao.getTasksForDate(date)

        // Put the fetched data into the cache
        cache.put(date, tasks)

        return tasks
    }

    suspend fun insert(task: Task) {
        // Perform database operation
        taskDao.insert(task)

        // Clear the cache as the data is modified
        cache.evictAll()
    }

    suspend fun update(task: Task) {
        // Perform database operation
        taskDao.update(task)

        // Clear the cache as the data is modified
        cache.evictAll()
    }
}
