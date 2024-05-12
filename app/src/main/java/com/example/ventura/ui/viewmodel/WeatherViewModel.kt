package com.example.ventura.ui.viewmodel

import android.app.Application
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
    private val isInternetAvailable = MutableLiveData<Boolean>()

    fun getWeather(context: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            val weatherResponse = model.getWeather(context, lat, lon)
            weatherLiveData.postValue(weatherResponse)
        }
    }

    fun checkInternetConnection() {
        viewModelScope.launch {
            val isConnected = model.checkInternetConnection()
            isInternetAvailable.postValue(isConnected)
        }
    }
}

class WeatherViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {

            // application context passed to use network Handler
            val model = WeatherModel(application)
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}