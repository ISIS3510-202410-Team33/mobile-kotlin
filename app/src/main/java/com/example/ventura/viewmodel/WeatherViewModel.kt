package com.example.ventura.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventura.data.network.CacheManager
import com.example.ventura.service.WeatherService
import com.example.ventura.model.WeatherResponse
import kotlinx.coroutines.launch
import okhttp3.Cache
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel(private val context: Context) : ViewModel() {
    val weatherLiveData = MutableLiveData<WeatherResponse>()
    private val API_ENDPOINT = "https://api.openweathermap.org/data/2.5/"
    private val API_KEY = "668c50fd98cdfe606ab0440ee65979c3"

    fun getWeather(lat: Double, lon: Double, context: Context) {
        viewModelScope.launch {
            var potentialResponse = CacheManager.getInstance(context).getWeather()
            var weatherResponse: WeatherResponse

            // there is no response
            // TODO: add empty response
            if (potentialResponse.equals(WeatherResponse.Nonexistent)) {
                Log.d("Cache decision", "get from network")

                val retrofit = Retrofit.Builder()
                    .baseUrl(API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(WeatherService::class.java)
                weatherResponse = service.getWeather(lat, lon, API_KEY)

                //prints the response
                Log.d("WeatherViewModel", weatherResponse.toString())

                CacheManager.getInstance(context).addWeather(weatherResponse)

            } else {
                Log.d("Cache decision", "return weather response from cache")
                weatherResponse = potentialResponse
            }

            weatherLiveData.postValue(weatherResponse)
        }
    }
}