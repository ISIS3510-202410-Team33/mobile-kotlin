// WeatherModel.kt
package com.example.ventura.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.ventura.service.WeatherService
import com.example.ventura.repository.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherModel {
    private val API_ENDPOINT = "https://api.openweathermap.org/data/2.5/"
    private val API_KEY = "668c50fd98cdfe606ab0440ee65979c3"

    suspend fun getWeather(context: Context, lat: Double, lon: Double): WeatherResponse? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        if (isConnected) {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(WeatherService::class.java)
            val weatherResponse = withContext(Dispatchers.IO) { service.getWeather(lat, lon, API_KEY) }

            Log.d("WeatherModel", weatherResponse.toString())
            return weatherResponse
        } else {
            Log.d("WeatherModel", "No internet connection")
            return null
        }
    }

    suspend fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}