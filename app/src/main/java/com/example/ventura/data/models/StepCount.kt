package com.example.ventura.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "step_count_table")
data class StepCount (
    @PrimaryKey @ColumnInfo(name = "dateOfMeasurement") val dateOfMeasurement: String,
    @ColumnInfo(name = "stepsAtDayStart") val stepsAtDayStart: Int,
    @ColumnInfo(name = "stepsAtNow") val stepsAtNow: Int
)
