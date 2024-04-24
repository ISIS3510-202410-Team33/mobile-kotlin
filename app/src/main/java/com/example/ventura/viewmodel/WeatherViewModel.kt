package com.example.ventura.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ventura.model.WeatherModel
import com.example.ventura.repository.WeatherResponse
import kotlinx.coroutines.launch

class WeatherViewModel(private val model: WeatherModel) : ViewModel() {
    val weatherLiveData = MutableLiveData<WeatherResponse?>()
    val isInternetAvailable = MutableLiveData<Boolean>()

    fun getWeather(context: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            val weatherResponse = model.getWeather(context, lat, lon)
            weatherLiveData.postValue(weatherResponse)
        }
    }

    fun checkInternetConnection(context: Context) {
        viewModelScope.launch {
            val isConnected = model.checkInternetConnection(context)
            isInternetAvailable.postValue(isConnected)
        }
    }
}

class WeatherViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            val model = WeatherModel()
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}