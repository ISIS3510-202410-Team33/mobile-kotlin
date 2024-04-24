package com.example.ventura.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ventura.ui.screens.ProfileScreen
import com.example.ventura.ui.theme.ThemeScreen
import com.example.ventura.viewmodel.ProfileViewModel
import com.example.ventura.viewmodel.ThemeViewModel

class NewProfileActivity : ComponentActivity(), SensorEventListener {
    private val profileViewModel: ProfileViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    private val DARKUPPERBOUND = 800
    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null
    private val SENSOR_DELAY_TURTLE = SensorManager.SENSOR_DELAY_NORMAL * 1000

    private fun isTooBright(brightness: Float): Boolean {
        return when (brightness.toInt()) {
            in 0..DARKUPPERBOUND -> false
            else -> true
        }
    }


    private fun setupLightSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]
            Log.d("light", light.toString())
            val newTooBright = isTooBright(light)

            themeViewModel.onNewBrightness(newTooBright)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }


    override fun onResume() {
        super.onResume()
        // TODO: observar themeViewModel para determinar cuándo prender y apagar el sensor
        sensorManager.registerListener(this, brightness, SENSOR_DELAY_TURTLE, SENSOR_DELAY_TURTLE)
        Log.d("main", "Fotosensor registrado")
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.d("main", "Fotosensor desregistrado")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setupLightSensor()
        Log.d("main", "Light sensor set")

        setContent {
            ThemeScreen(themeViewModel = themeViewModel) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProfileScreen(
                        profileViewModel = profileViewModel,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewLight() {
    ThemeScreen(darkTheme=false) {
        ProfileScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewDark() {
    ThemeScreen(darkTheme = true) {
        ProfileScreen()
    }
}