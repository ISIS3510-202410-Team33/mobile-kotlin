package com.example.ventura.model.data

import java.time.LocalDate

data class StepCount (
    val steps: Int = 0,
    val dateOfMeasurement: LocalDate = LocalDate.now()
)