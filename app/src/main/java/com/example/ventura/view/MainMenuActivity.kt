package com.example.ventura.view

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.ventura.R
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class MainMenuActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val featureCrashHandler = FeatureCrashHandler("main_menu")


    override fun onCreate(savedInstanceState: Bundle?) {
        try {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_menu)

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Mostrar un diálogo de alerta al usuario
                mostrarDialogoGPS()
            }

            // requestStoragePermission()
            val notificationButton = findViewById<ImageView>(R.id.notification_icon)
            notificationButton.setOnClickListener{
                val intent = Intent(this, NotificationsActivity::class.java )
                startActivity(intent)
                }


            // Recuperar el correo del usuario de los extras del intent
            val userEmail = intent.getStringExtra("user_email")


            Log.d("screen-flow", "¡Welcome, $userEmail!")

            // Populate the tag welcome with the username
            val welcomeTextView = findViewById<TextView>(R.id.textView5)
            welcomeTextView.text = "Hi, ${extractUsername(userEmail)}!"

            // Populate todays date with the current date
            val dateTextView = findViewById<TextView>(R.id.textView6)
            val currentDate = java.util.Calendar.getInstance().time
            
            // get only the date, example: Sun Apr 14, get it split by space and get the first 3 elements
            val date = currentDate.toString().split(" ").subList(0, 3).joinToString(" ")
            dateTextView.text = date
            
            

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
            var weatherDescription = weatherResponse.weather[0].description.toLowerCase()

            // ARTIFICIALLY CHANGES THE WEATHER FOR TESTING PURPOSES
            // weatherDescription = "rain"

            // obtain the relative layout
            val relativeLayout = findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)

            val weatherIconResource = when {
                weatherDescription.contains("rain") || weatherDescription.contains("drizzle") -> {
                    // change the background of the RelativeLayout from round_corners  to round_corners_rain
                    relativeLayout.setBackgroundResource(R.drawable.rounded_corners_rain)
                    R.drawable.cloud_with_rain
                }
                weatherDescription.contains("cloud") -> {
                    R.drawable.cloud
                }
                weatherDescription.contains("clear") -> {
                    // change the background of the RelativeLayout from round_corners  to round_corners_sun
                    relativeLayout.setBackgroundResource(R.drawable.rounded_corners_sun)
                    R.drawable.sun_behind_cloud
                }
                else -> R.drawable.cloud
            }
            weatherIconImageView.setImageResource(weatherIconResource)
            humidityTextView.text = "Humidity: $humidity%"

            // Set the weather message based on weather description
            if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
                weatherMessageTextView.text = "<b>Watch out! It's raining heavily</b>"
                sendRainNotification()
            } else {
                weatherMessageTextView.text = "Weather seems fine today!"
            }
            weatherMessageTextView.setText(Html.fromHtml(weatherMessageTextView.text.toString()), TextView.BufferType.SPANNABLE)
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

    private fun sendRainNotification() {
        Log.d("Notification", "Sending rain notification")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "RAIN_NOTIFICATION_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId, "Rain Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.cloud_with_rain)
            .setContentTitle("Weather Alert")
            .setContentText("Watch out! It's raining heavily")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(0, notificationBuilder.build())
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

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
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

    // Extract username from email (part before the "@")
    private fun extractUsername(email: String?): String {
        return email?.substringBefore("@") ?: ""
    }
}