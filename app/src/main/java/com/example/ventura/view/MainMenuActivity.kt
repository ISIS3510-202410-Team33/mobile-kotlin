package com.example.ventura.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.ventura.R
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class MainMenuActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val featureCrashHandler = FeatureCrashHandler("main_menu")
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.new_main_menu)

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Mostrar un diálogo de alerta al usuario
                mostrarDialogoGPS()
            }

            videoView = findViewById(R.id.videoView)
            videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.video2))
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
            }
            videoView.start()

            val notificationButton = findViewById<ImageView>(R.id.notification_icon)
            notificationButton.setOnClickListener{
                val intent = Intent(this, NotificationsActivity::class.java )
                startActivity(intent)
                }


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
            //TODO: Glide.with(this).load(weatherIconUrl).into(weatherIconImageView)

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
            val logOutButton = findViewById<TextView>(R.id.log_out)

            logOutButton.setOnClickListener {
                // Eliminar las credenciales persistidas
                clearCredentials()

                // Mostrar un mensaje de éxito
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()

                // Redirigir a la actividad de inicio de sesión
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                // Mostrar un mensaje de éxito
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()

                finish() // Cierra la actividad actual para evitar que el usuario regrese presionando el botón "Atrás"

                // Mostrar un mensaje de éxito
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()

            }

        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_email", userEmail)
            startActivity(intent)

        }

        

            buttonMap.setOnClickListener {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("user_email", userEmail) // Aquí pasamos el correo como un extra
                startActivity(intent)
            }



            buttonSettings.setOnClickListener {
                // Navigate to Settings Activity
            }
        } catch (e: Exception) { featureCrashHandler.logCrash("display", e); }
    }

    override fun onResume() {
        super.onResume()
        // Iniciar la reproducción del video
        videoView.start()
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

    private fun mostrarDialogoGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("GPS Desactivado")
            .setMessage("Para usar esta aplicación, debes activar el GPS.")
            .setCancelable(false)
            .setPositiveButton("Activar GPS") { dialog, _ ->
                // Abrir la configuración de ubicación del dispositivo
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Salir") { dialog, _ ->
                dialog.dismiss()
                finish() // Opcional: cierra la actividad si el usuario decide salir
            }
        val alert = builder.create()
        alert.show()
    }

    private fun clearCredentials() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("email") // Eliminar el correo electrónico guardado
        // Si tienes más datos a eliminar, puedes agregar más líneas aquí
        editor.apply()
    }


}