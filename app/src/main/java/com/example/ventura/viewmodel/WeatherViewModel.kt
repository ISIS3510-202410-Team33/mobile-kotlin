package com.example.ventura.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventura.service.WeatherService
import com.example.ventura.model.WeatherResponse
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel : ViewModel() {
    val weatherLiveData = MutableLiveData<WeatherResponse>()
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
}