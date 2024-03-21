package com.example.ventura.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.example.ventura.R

import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.ventura.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.bumptech.glide.Glide
class MainMenuActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        Log.d("MainMenuActivity", "onCreate")

        val weatherTextView = findViewById<TextView>(R.id.weatherTextView)
        val cityTextView = findViewById<TextView>(R.id.cityTextView)
        val weatherIconImageView = findViewById<ImageView>(R.id.weatherIconImageView)
        val humidityTextView = findViewById<TextView>(R.id.humidityTextView)
        val temperatureTextView = findViewById<TextView>(R.id.temperatureTextView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        weatherViewModel.weatherLiveData.observe(this, Observer { weatherResponse ->
            cityTextView.text = "City: Bogotá"
            val tempInCelsius = weatherResponse.main.temp - 273.15
            val humidity = weatherResponse.main.humidity
            val formattedTemp = String.format("%.1f", tempInCelsius)
            weatherTextView.text = "Weather: ${weatherResponse.weather[0].description}"
            temperatureTextView.text = "Temperature: ${formattedTemp}°C"
            val weatherIconUrl = when (weatherResponse.weather[0].description){
                "Clear" -> "https://emojiapi.dev/api/v1/sun/512.png"
                "Clouds" -> "https://emojiapi.dev/api/v1/cloud/512.png"
                "Rain" -> "https://emojiapi.dev/api/v1/rain/512.png"
                else -> "https://emojiapi.dev/api/v1/question_mark/512.png"
            }
            humidityTextView.text = "Humidity: $humidity%"
            Glide.with(this).load(weatherIconUrl).into(weatherIconImageView)
        })

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // Permissions are already granted, get the location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    weatherViewModel.getWeather(location.latitude, location.longitude)
                } else {
                    // Handle the situation when location data is null
                    // Use Bogota, Colombia as the default city
                    val defaultLatitude = 4.7110
                    val defaultLongitude = -74.0721
                    weatherViewModel.getWeather(defaultLatitude, defaultLongitude)
                }

            }
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                weatherViewModel.getWeather(location.latitude, location.longitude)
            }
        }

        val buttonProfile = findViewById<Button>(R.id.buttonProfile)
        val buttonMap = findViewById<Button>(R.id.buttonMap)
        val buttonSettings = findViewById<Button>(R.id.buttonSettings)

        buttonProfile.setOnClickListener {
            // Navigate to Profile Activity
        }

        buttonMap.setOnClickListener {
            // Navigate to Map Activity
        }

        buttonSettings.setOnClickListener {
            // Navigate to Settings Activity
        }
    }
}