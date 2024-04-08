package com.example.ventura.model

sealed class WeatherResponse(val weather : List<Weather>, val main: Main) {

    // nonexistent class to avoid null manipulation
    object Nonexistent : WeatherResponse(emptyList(), Main.Nonexistent) {
        override fun toString(): String {
            return "Weather data unavailable"
        }
    }

    data class Data(val actualData: WeatherResponse) : WeatherResponse(actualData.weather, actualData.main)
}


data class Weather(
    val description: String
)

// FIX: ambiguous name
sealed class Main(val temp: Double, val humidity: Double) {

    // nonexistent class to avoid null manipulation
    object Nonexistent : Main(0.0, 0.0) {
        override fun toString(): String {
            return "Main data unavailable"
        }
    }

    data class Data(val actualData: Main) : Main(actualData.temp, actualData.humidity)
}
