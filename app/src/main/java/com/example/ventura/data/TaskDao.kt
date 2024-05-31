package com.example.ventura.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ventura.model.Task
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query("SELECT * FROM task WHERE date = :date")
    suspend fun getTasksForDate(date: LocalDate): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)
}
