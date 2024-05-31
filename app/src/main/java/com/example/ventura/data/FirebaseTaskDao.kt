package com.example.ventura.data

import com.example.ventura.model.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FirebaseTaskDao() {
    private val storage = FirebaseStorage.getInstance()
    private val tasksBucket: StorageReference = storage.getReferenceFromUrl("gs://ventura-bfe66.appspot.com")

    suspend fun getTasksForDate(date: LocalDate): List<Task> {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tasksFile = tasksBucket.child("$dateStr.json")
        return try {
            val json = tasksFile.getBytes(Long.MAX_VALUE).await().toString(Charsets.UTF_8)
            Gson().fromJson(json, Array<Task>::class.java).toList()
        } catch (e: StorageException) {
            // Return an empty list if the file does not exist
            emptyList()
        } catch (e: UnknownHostException) {
            // Return an empty list if there is no internet connection
            emptyList()
        }
    }

    suspend fun insert(task: Task) {
        val dateStr = task.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tasksFile = tasksBucket.child("$dateStr.json")

        // Fetch existing tasks
        val existingTasks = getTasksForDate(task.date)

        // Add the new task
        val updatedTasks = existingTasks + task

        // Update the tasks file
        tasksFile.putBytes(Gson().toJson(updatedTasks).toByteArray()).await()
    }

    suspend fun update(task: Task) {
        val dateStr = task.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tasksFile = tasksBucket.child("$dateStr.json")

        // Fetch existing tasks
        val existingTasks = getTasksForDate(task.date).toMutableList()

        // Find and update the task
        val taskIndex = existingTasks.indexOfFirst { it.id == task.id }
        if (taskIndex != -1) {
            existingTasks[taskIndex] = task
        }

        // Update the tasks file
        tasksFile.putBytes(Gson().toJson(existingTasks).toByteArray()).await()
    }
}