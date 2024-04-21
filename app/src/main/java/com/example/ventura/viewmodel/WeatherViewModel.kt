package com.example.ventura.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventura.service.WeatherService
import com.example.ventura.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel : ViewModel() {
    val weatherLiveData = MutableLiveData<WeatherResponse>()
    val isInternetAvailable = MutableLiveData<Boolean>()
    private val API_ENDPOINT = "https://api.openweathermap.org/data/2.5/"
    private val API_KEY = "668c50fd98cdfe606ab0440ee65979c3"

    fun getWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(WeatherService::class.java)
            val weatherResponse = service.getWeather(lat, lon, API_KEY)

            //prints the response
            Log.d("WeatherViewModel", weatherResponse.toString())

            weatherLiveData.postValue(weatherResponse)
        }
    }

    fun checkInternetConnection(context: Context) {
        viewModelScope.launch {
            while (true) {
                delay(1000) // Check every second
                val isConnected = withContext(Dispatchers.IO) {
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val network = connectivityManager.activeNetwork ?: return@withContext false
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
                isInternetAvailable.postValue(isConnected)
            }
        }
    }
}