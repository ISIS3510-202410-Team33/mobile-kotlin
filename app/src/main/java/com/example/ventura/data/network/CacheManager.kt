package com.example.ventura.data.network

import android.content.Context
import android.util.Log
import com.example.ventura.model.Location
import com.example.ventura.model.WeatherResponse
import com.example.ventura.service.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CacheManager(context: Context) {

    // caché de respuesta del clima
    private lateinit var weatherResponseCached: WeatherResponse;

    companion object {
        var instance: CacheManager? = null
        fun getInstance(context: Context) =
            instance ?: CacheManager(context).also {
                instance = it
            }
    }

    fun addWeather(weatherResponse: WeatherResponse) {
        weatherResponseCached = weatherResponse
        Log.d("Cached weather response", weatherResponse.toString())
    }

    fun getWeather(): WeatherResponse {
        return weatherResponseCached
    }
}
