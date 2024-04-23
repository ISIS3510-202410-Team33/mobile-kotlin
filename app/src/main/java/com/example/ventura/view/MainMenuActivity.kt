package com.example.ventura.view

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.example.ventura.R
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.util.concurrent.atomic.AtomicBoolean
import com.google.android.gms.location.LocationServices

class MainMenuActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val featureCrashHandler = FeatureCrashHandler("main_menu")
    private val isRunningThread = AtomicBoolean(true)
    var currentConnection = "ok"


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_menu)
            

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mostrarDialogoGPS()
            }

            val notificationButton = findViewById<ImageView>(R.id.notification_icon)
            notificationButton.setOnClickListener{
                val intent = Intent(this, NotificationsActivity::class.java )
                startActivity(intent)
            }

            val userEmail = intent.getStringExtra("user_email")

            Log.d("screen-flow", "¡Welcome, $userEmail!")

            val welcomeTextView = findViewById<TextView>(R.id.textView5)
            welcomeTextView.text = "Hi, ${extractUsername(userEmail)}!"

            val dateTextView = findViewById<TextView>(R.id.textView6)
            val currentDate = java.util.Calendar.getInstance().time
            val date = currentDate.toString().split(" ").subList(0, 3).joinToString(" ")
            dateTextView.text = date

            locationRequest = LocationRequest.create().apply {
                interval = 20000
                fastestInterval = 10000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        Log.d("Location", "$location")
                        weatherViewModel.getWeather(this@MainMenuActivity, location.latitude, location.longitude)
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

                val relativeLayout = findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)

                val weatherIconResource = when {
                    weatherDescription.contains("rain") || weatherDescription.contains("drizzle") -> {
                        relativeLayout.setBackgroundResource(R.drawable.rounded_corners_rain)
                        R.drawable.cloud_with_rain
                    }
                    weatherDescription.contains("cloud") -> {
                        relativeLayout.setBackgroundResource(R.drawable.rounded_corners)
                        R.drawable.cloud
                    }
                    weatherDescription.contains("clear") -> {
                        relativeLayout.setBackgroundResource(R.drawable.rounded_corners_sun)
                        R.drawable.sun_behind_cloud
                    }
                    else -> R.drawable.cloud
                }
                weatherIconImageView.setImageResource(weatherIconResource)
                humidityTextView.text = "Humidity: $humidity%"

                if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
                    weatherMessageTextView.text = "<b>Watch out! It's raining heavily</b>"
                    sendRainNotification()
                } else {
                    weatherMessageTextView.text = "Weather seems fine today!"
                }
                weatherMessageTextView.setText(Html.fromHtml(weatherMessageTextView.text.toString()), TextView.BufferType.SPANNABLE)
            })

            Thread {
                while (isRunningThread.get()) {
                    val isConnected = isInternetAvailable(this)
                    runOnUiThread {
                        
                        // sends a log message to the Logcat
                        Log.d("Internet", "Internet connection: $isConnected")

                        if (isConnected) {
                            if (currentConnection == "offline") {
                                Toast.makeText(this, "Connection restored, weather info will show up shortly", Toast.LENGTH_SHORT).show()
                                currentConnection = "ok"
                            }
                            if (ActivityCompat.checkSelfPermission(
                                    this, Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(
                                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    1
                                )
                            } else {
                                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                            }
                        } else {
                            if (currentConnection == "ok") {
                                Toast.makeText(this, "No internet connection, cannot fetch weather info", Toast.LENGTH_SHORT).show()
                                sendOfflineNotification()
                                currentConnection = "offline"
                            }
                            weatherTextView.text = "No internet connection"
                            temperatureTextView.text = "Cannot fetch weather info"
                            humidityTextView.text = "Please try again later"
                            cityTextView.text = ""
                            weatherMessageTextView.text = ""


                            // change the icon and background to indicate offline status
                            val weatherIconResource = R.drawable.error
                            val relativeLayout = findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)
                            relativeLayout.setBackgroundResource(R.drawable.rounded_corners)
                            weatherIconImageView.setImageResource(weatherIconResource)


                        }
                    }

                    Thread.sleep(5000)

                }
            }.start()

            val buttonProfile = findViewById<Button>(R.id.buttonProfile)
            val buttonMap = findViewById<Button>(R.id.buttonMap)
            val buttonSettings = findViewById<Button>(R.id.buttonSettings)
            val logOutButton = findViewById<TextView>(R.id.log_out)

            logOutButton.setOnClickListener {
                clearCredentials()
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()
                finish()
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()
            }

            buttonProfile.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("user_email", userEmail)
                startActivity(intent)
            }

            buttonMap.setOnClickListener {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("user_email", userEmail)
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

    private fun sendOfflineNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "OFFLINE_NOTIFICATION_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId, "Offline Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.error)
            .setContentTitle("Offline Alert")
            .setContentText("You're offline. Unable to fetch weather info.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, notificationBuilder.build())
    }

    override fun onResume() {
        super.onResume()
        weatherViewModel.checkInternetConnection(this)
    }

    override fun onPause() {
        try {
            super.onPause()
            isRunningThread.set(false)
            stopLocationUpdates()
        } catch (e: Exception) { featureCrashHandler.logCrash("pause", e); }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun mostrarDialogoGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("GPS Disabled")
            .setMessage("Please enable GPS to get weather information")
            .setCancelable(false)
            .setPositiveButton("Enable GPS") { dialog, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun clearCredentials() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("email")
        editor.apply()
    }

    private fun extractUsername(email: String?): String {
        return email?.substringBefore("@") ?: ""
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}