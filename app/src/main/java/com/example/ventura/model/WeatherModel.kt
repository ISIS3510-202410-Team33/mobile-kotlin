// WeatherModel.kt
package com.example.ventura.model

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.ventura.PermanentSensorsApplication
import com.example.ventura.repository.WeatherResponse
import com.example.ventura.service.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val TAG = "WeatherModel"


class WeatherModel(
    private val application: Application
) {
    private val API_ENDPOINT = "https://api.openweathermap.org/data/2.5/"
    private val API_KEY = "668c50fd98cdfe606ab0440ee65979c3"
    private val networkHandler by lazy { (application as PermanentSensorsApplication).networkHandler }


    /**
     * Returns the weather
     */
    suspend fun getWeather(context: Context, lat: Double, lon: Double): WeatherResponse? {
        return if (networkHandler.isInternetAvailable()) {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(WeatherService::class.java)
            val weatherResponse = withContext(Dispatchers.IO) { service.getWeather(lat, lon, API_KEY) }

            Log.d(TAG, weatherResponse.toString())
            weatherResponse
        } else {
            Log.d(TAG, "No internet connection")
            null
        }
    }


    /**
     * TODO: Should this be pulled off the view model?
     */
    fun checkInternetConnection(): Boolean {
        return networkHandler.isInternetAvailable()
    }
}