package com.example.ventura.ui.activity

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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.ventura.PermanentSensorsApplication
import com.example.ventura.R
import com.example.ventura.ui.viewmodel.WeatherViewModel
import com.example.ventura.ui.viewmodel.WeatherViewModelFactory
import com.example.ventura.utils.FeatureCrashHandler
import com.example.ventura.utils.NetworkHandler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class MainMenuActivity : AppCompatActivity() {
    private lateinit var app: PermanentSensorsApplication
    // app context is passed in order to
    private lateinit var weatherViewModel: WeatherViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var coursesLayout: FrameLayout
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val featureCrashHandler = FeatureCrashHandler("main_menu")
    private var currentConnection = "ok"
    private var sentWeatherNotification = false
    private var activityAlreadyDestroyed = false


    // network utility
    private lateinit var networkHandler: NetworkHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)

            app = application as PermanentSensorsApplication
            networkHandler = app.networkHandler
            weatherViewModel = ViewModelProvider(
                this,
                WeatherViewModelFactory(app)
            )[WeatherViewModel::class.java]

            setContentView(R.layout.activity_main_menu)
            val bannerUniandes = findViewById<TextView>(R.id.textView3)
            val userEmail = intent.getStringExtra("user_email")

            coursesLayout = findViewById(R.id.coursesLayout)

            coursesLayout.setOnClickListener{
                val intent = Intent(this, CoursesActivity::class.java )
                intent.putExtra("user_email", userEmail)
                startActivity(intent)
            }


            bannerUniandes.setOnClickListener{
                val intent = Intent(this, CampusImagesActivity::class.java)
                startActivity(intent)
            }

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mostrarDialogoGPS()
            }

            val notificationButton = findViewById<ImageView>(R.id.notification_icon)
            notificationButton.setOnClickListener{
                val intent = Intent(this, NotificationsActivity::class.java )
                startActivity(intent)
            }



            Log.d("screen-flow", "¡Welcome, $userEmail!")

            val welcomeTextView = findViewById<TextView>(R.id.textView5)
            welcomeTextView.text = "Hi, ${extractUsername(userEmail)}!"

            val dateTextView = findViewById<TextView>(R.id.textView6)
            val currentDate = java.util.Calendar.getInstance().time
            val date = currentDate.toString().split(" ").subList(0, 3).joinToString(" ")
            dateTextView.text = date

            locationRequest = LocationRequest.create().apply {
                interval = 10000
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

            if (!isFinishing && !isDestroyed) {
                Glide.with(this).load(R.drawable.load3).into(weatherIconImageView)
            }
            weatherViewModel.weatherLiveData.observe(this, Observer { weatherResponse ->

                if (weatherResponse != null) {
                    cityTextView.text = "City: Bogotá"
                    val tempInCelsius = weatherResponse!!.main.temp - 273.15
                    val humidity = weatherResponse.main.humidity
                    val formattedTemp = String.format("%.1f", tempInCelsius)
                    weatherTextView.text = "Weather: ${weatherResponse.weather[0].description}"
                    temperatureTextView.text = "Temperature: ${formattedTemp}°C"
                    var weatherDescription = weatherResponse.weather[0].description.toLowerCase()

                    val relativeLayout =
                        findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)

                    val weatherIconResource = when {
                        weatherDescription.contains("rain") || weatherDescription.contains("drizzle") -> {
                            relativeLayout.setBackgroundResource(R.drawable.rounded_corners_rain)
                            R.drawable.cloud_with_rain
                        }

                        weatherDescription.contains("cloud") -> {
                            relativeLayout.setBackgroundResource(R.drawable.rounded_corners_cloud)
                            R.drawable.cloud
                        }

                        weatherDescription.contains("clear") -> {
                            relativeLayout.setBackgroundResource(R.drawable.rounded_corners_sun)
                            R.drawable.sun_behind_cloud
                        }

                        else -> R.drawable.cloud
                    }
                    if (!isFinishing && !isDestroyed) {
                        
                        Glide.with(this).load(weatherIconResource).into(weatherIconImageView)
                    }
                    humidityTextView.text = "Humidity: $humidity%"

                    if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
                        weatherMessageTextView.text = "<b>Watch out! It's raining heavily in your area</b>"
                        if (!sentWeatherNotification){
                            sendRainNotification()
                            sentWeatherNotification = true
                        }
                    } else {
                        weatherMessageTextView.text = "Weather seems fine today!"
                    }
                    weatherMessageTextView.setText(
                        Html.fromHtml(weatherMessageTextView.text.toString()),
                        TextView.BufferType.SPANNABLE
                    )

                } else {

                    if (currentConnection == "ok") {
                        Toast.makeText(this, "You have lost internet connection", Toast.LENGTH_SHORT).show()
                        // sendOfflineNotification()
                        currentConnection = "offline"
                    }
                    weatherTextView.text = "You're offline"
                    temperatureTextView.text = "No weather info available"
                    humidityTextView.text = "Please try again later"
                    cityTextView.text = ""
                    weatherMessageTextView.text = ""


                    // change the icon and background to indicate offline status
                    val weatherIconResource = R.drawable.error
                    val relativeLayout = findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)
                    relativeLayout.setBackgroundResource(R.drawable.rounded_corners)
                    if (!isFinishing && !isDestroyed) {

                        Glide.with(this).load(weatherIconResource).into(weatherIconImageView)
                    }

                }


            })

            Thread {
                while (!Thread.currentThread().isInterrupted && !activityAlreadyDestroyed){
                    val isConnected = networkHandler.isInternetAvailable()
                    runOnUiThread {

                        // sends a log message to the Logcat
                        Log.d("Internet", "Internet connection: $isConnected")

                        if (isConnected) {
                            if (currentConnection == "offline") {
                                Toast.makeText(
                                    this,
                                    "Connection restored",
                                    Toast.LENGTH_SHORT
                                ).show()
                                currentConnection = "ok"


                                weatherTextView.text = "Loading weather data..."
                                temperatureTextView.text = "Loading weather data..."
                                humidityTextView.text = "Loading weather data..."
                                cityTextView.text = "Loading city..."

                                // change the icon and background to indicate offline status
                                val weatherIconResource = R.drawable.load3
                                val relativeLayout =
                                    findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)
                                relativeLayout.setBackgroundResource(R.drawable.rounded_corners)
                                if (!isFinishing && !isDestroyed) {

                                    Glide.with(this).load(weatherIconResource)
                                        .into(weatherIconImageView)
                                }

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
                                fusedLocationClient.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback,
                                    null
                                )
                            }

                            // checks permission for physical activity / step sensor
                            if (ActivityCompat.checkSelfPermission(
                                    this, Manifest.permission.ACTIVITY_RECOGNITION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                                    1
                                )
                            }

                            // checks permission for camera / light sensor
                            if (ActivityCompat.checkSelfPermission(
                                    this, Manifest.permission.CAMERA
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.CAMERA),
                                    1
                                )
                            }


                        } else {
                            if (currentConnection == "ok") {
                                Toast.makeText(
                                    this,
                                    "You have lost internet connection",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // sendOfflineNotification()
                                currentConnection = "offline"
                            }
                            weatherTextView.text = "You're offline"
                            temperatureTextView.text = "No weather info available"
                            humidityTextView.text = "Please try again later"
                            cityTextView.text = ""
                            weatherMessageTextView.text = ""


                            // change the icon and background to indicate offline status
                            val weatherIconResource = R.drawable.error
                            val relativeLayout =
                                findViewById<RelativeLayout>(R.id.weatherInfoRelativeLayout)
                            relativeLayout.setBackgroundResource(R.drawable.rounded_corners)
                            if (!isFinishing && !isDestroyed) {

                                Glide.with(this).load(weatherIconResource)
                                    .into(weatherIconImageView)
                            }


                        }
                    }

                    try {
                        Thread.sleep(5000)
                    } catch (e: InterruptedException) {
                        // Restore the interrupted status
                        Thread.currentThread().interrupt()
                    }

                }
            }.start()

            val buttonProfile = findViewById<Button>(R.id.buttonProfile)
            val buttonMap = findViewById<Button>(R.id.buttonMap)
            val buttonSettings = findViewById<Button>(R.id.buttonSettings)
            val logOutButton = findViewById<TextView>(R.id.log_out)

            logOutButton.setOnClickListener {
                clearCache(this)
                clearCredentials()
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show()
                val intent = if (networkHandler.isInternetAvailable()) {
                    Intent(this, LoginActivity::class.java)
                } else {
                    Intent(this, NoInternetLogin::class.java)
                }
                startActivity(intent)
                finish()
            }

            buttonProfile.setOnClickListener {
                val intent = Intent(this, NewProfileActivity::class.java)
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

    override fun onResume() {
        super.onResume()
        weatherViewModel.checkInternetConnection()

    }

    override fun onPause() {
        try {
            super.onPause()
            stopLocationUpdates()

            // interrupts the current thread in order to avoid memory leaks
            Thread.currentThread().interrupt()

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


    override fun onDestroy() {
        super.onDestroy()
        clearCache(this)

        // interrupts the current thread in order to avoid memory leaks
        Thread.currentThread().interrupt()
        activityAlreadyDestroyed = true


    }

    private fun clearCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir.deleteRecursively()
            Log.d("Cache", "Cache cleared successfully")
        } catch (e: Exception) {
            Log.e("Cache", "Failed to clear cache: ${e.message}")
        }
    }
}