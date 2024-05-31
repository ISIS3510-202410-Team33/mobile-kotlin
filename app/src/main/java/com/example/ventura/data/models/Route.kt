package com.example.ventura.data.models

data class Route(
    val sites: List <Site> = emptyList(),
    val distance: Int = -1
)
