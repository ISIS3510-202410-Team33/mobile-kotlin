package com.example.ventura.model

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main
)

data class Weather(
    val description: String
)

data class Main(
    val temp: Double
)