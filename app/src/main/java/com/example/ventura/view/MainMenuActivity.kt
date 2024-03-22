package com.example.ventura.view

import android.Manifest
import android.content.Intent
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class MainMenuActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)


        // Recuperar el correo del usuario de los extras del intent
        val userEmail = intent.getStringExtra("user_email")


        Log.d("screen-flow", "¡Bienvenido, $userEmail!")

        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Set the desired interval for active location updates, in milliseconds.
            fastestInterval = 5000 // Set the fastest rate for active location updates, in milliseconds.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority of the request.
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // Call a method to handle the current location.
                    Log.d("Location", "$location")
                    weatherViewModel.getWeather(location.latitude, location.longitude)
                }
            }
        }

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
        })

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // Permissions are already granted, start location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        val buttonProfile = findViewById<Button>(R.id.buttonProfile)
        val buttonMap = findViewById<Button>(R.id.buttonMap)
        val buttonSettings = findViewById<Button>(R.id.buttonSettings)

        buttonProfile.setOnClickListener {

        }

        buttonMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("user_email", userEmail) // Aquí pasamos el correo como un extra
            startActivity(intent)
            finish()
        }



        buttonSettings.setOnClickListener {
            // Navigate to Settings Activity
        }
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}