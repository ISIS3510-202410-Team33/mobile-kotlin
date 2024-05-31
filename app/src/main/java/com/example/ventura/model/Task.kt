package com.example.ventura.model

import java.time.LocalDate

data class Task(
    val id: Int,
    val date: LocalDate,
    val title: String,
    val description: String
)
