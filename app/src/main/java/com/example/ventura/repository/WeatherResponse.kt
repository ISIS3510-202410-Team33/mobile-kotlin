package com.example.ventura.repository

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main
)

data class Weather(
    val description: String
)

data class Main(
    val temp: Double,
    val humidity: Double
)