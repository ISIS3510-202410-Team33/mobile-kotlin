package com.example.ventura.data.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Data class to represent the data binding
 * for StepCount View and ViewModel
 */
data class StepCountUi (
    val steps: Int = 0,
    val dateOfMeasurement: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
)
