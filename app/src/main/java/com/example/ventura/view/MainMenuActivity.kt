package com.example.ventura.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.example.ventura.R
import com.example.ventura.view.ProfileActivity
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
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class MainMenuActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val featureCrashHandler = FeatureCrashHandler("main_menu");


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_menu)


            // Recuperar el correo del usuario de los extras del intent
            val userEmail = intent.getStringExtra("user_email")


            Log.d("screen-flow", "¡Bienvenido, $userEmail!")

            locationRequest = LocationRequest.create().apply {
                interval =
                    10000 // Set the desired interval for active location updates, in milliseconds.
                fastestInterval =
                    5000 // Set the fastest rate for active location updates, in milliseconds.
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority of the request.
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
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
        val weatherMessageTextView = findViewById<TextView>(R.id.weatherMessageTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        weatherViewModel.weatherLiveData.observe(this, Observer { weatherResponse ->
            cityTextView.text = "City: Bogotá"
            val tempInCelsius = weatherResponse.main.temp - 273.15
            val humidity = weatherResponse.main.humidity
            val formattedTemp = String.format("%.1f", tempInCelsius)
            weatherTextView.text = "Weather: ${weatherResponse.weather[0].description}"
            temperatureTextView.text = "Temperature: ${formattedTemp}°C"
            val weatherDescription = weatherResponse.weather[0].description.toLowerCase()
            val weatherIconUrl = when {
                weatherDescription.contains("rain") || weatherDescription.contains("drizzle") ->
                    "https://emojiapi.dev/api/v1/cloud_with_rain/512.png"
                weatherDescription.contains("cloud") ->
                    "https://emojiapi.dev/api/v1/cloud/512.png"
                weatherDescription.contains("clear") ->
                    "https://emojiapi.dev/api/v1/sun_behind_cloud/512.png"
                else -> "https://emojiapi.dev/api/v1/cloud/512.png"
            }
            Glide.with(this).load(weatherIconUrl).into(weatherIconImageView)

            humidityTextView.text = "Humidity: $humidity%"

            // Set the weather message based on weather description
            weatherMessageTextView.text = if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
                "Watch out! It's raining heavily."
            } else {
                "Weather seems fine today!"
            }
        })

            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                // Permissions are already granted, start location updates
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }

            val buttonProfile = findViewById<Button>(R.id.buttonProfile)
            val buttonMap = findViewById<Button>(R.id.buttonMap)
            val buttonSettings = findViewById<Button>(R.id.buttonSettings)


        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_email", userEmail)
            startActivity(intent)
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
        } catch (e: Exception) { featureCrashHandler.logCrash("display", e); }
    }


    override fun onPause() {
        try {
            super.onPause()
            stopLocationUpdates()
        } catch (e: Exception) { featureCrashHandler.logCrash("pause", e); }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}