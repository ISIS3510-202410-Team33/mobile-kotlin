package com.example.ventura.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ventura.data.models.StepCount
import kotlinx.coroutines.flow.Flow


@Dao
interface StepCountDao {

    @Query("SELECT * FROM step_count_table WHERE dateOfMeasurement = :date")
    fun findByDateOfMeasurement(date: String): Flow<StepCount>

    @Update()
    suspend fun updateStepCount(stepCount: StepCount)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepCount(stepCount: StepCount)


    @Query("UPDATE step_count_table SET stepsAtNow = :stepsAtNow WHERE dateOfMeasurement = :date")
    suspend fun updateStepsAtNow(date: String, stepsAtNow: Int): Int
}