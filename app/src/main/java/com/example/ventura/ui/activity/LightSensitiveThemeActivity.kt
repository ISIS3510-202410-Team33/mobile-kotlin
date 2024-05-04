package com.example.ventura.ui.activity

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ventura.ui.viewmodel.ThemeViewModel


private val TAG = "SENSITIVE_THEME_ACTIVITY"


// TODO: open es demasiado abierto?
open class LightSensitiveThemeActivity : ComponentActivity(), SensorEventListener {
    val themeViewModel: ThemeViewModel by viewModels()

    private val DARKUPPERBOUND = 500
    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null
    private val SENSOR_DELAY_TURTLE = SensorManager.SENSOR_DELAY_NORMAL * 5_000_000

    private fun setupLightSensor() {
        Log.d(TAG, "Setting up sensors...")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }


    private fun isTooBright(brightness: Float): Boolean {
        Log.d(TAG, "Is too bright?")
        return when (brightness.toInt()) {
            in 0..DARKUPPERBOUND -> false
            else -> true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Creating...")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupLightSensor()
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Fotosensor desregistrado")
    }


    override fun onResume() {
        super.onResume()
        // TODO: observar themeViewModel para determinar cuándo prender y apagar el sensor
        sensorManager.registerListener(this, brightness, SENSOR_DELAY_TURTLE)
        Log.d(TAG, "Fotosensor registrado")
    }


    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "Sensor changed...")
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]
            Log.d(TAG, "Light: $light")
            val newTooBright = isTooBright(light)

            themeViewModel.onNewBrightness(newTooBright)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

}