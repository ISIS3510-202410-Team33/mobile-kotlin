package com.example.ventura.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ventura.viewmodel.ThemeViewModel


// TODO: open es demasiado abierto?
open class AdaptiveThemeActivity : ComponentActivity(), SensorEventListener {
    val themeViewModel: ThemeViewModel by viewModels()

    private val DARKUPPERBOUND = 500
    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null
    private val SENSOR_DELAY_TURTLE = SensorManager.SENSOR_DELAY_NORMAL * 5_000_000

    private fun setupLightSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }


    private fun isTooBright(brightness: Float): Boolean {
        return when (brightness.toInt()) {
            in 0..DARKUPPERBOUND -> false
            else -> true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupLightSensor()
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.d("main", "Fotosensor desregistrado")
    }


    override fun onResume() {
        super.onResume()
        // TODO: observar themeViewModel para determinar cuándo prender y apagar el sensor
        sensorManager.registerListener(this, brightness, SENSOR_DELAY_TURTLE)
        Log.d("main", "Fotosensor registrado")
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]
            Log.d("light", light.toString())
            val newTooBright = isTooBright(light)

            themeViewModel.onNewBrightness(newTooBright)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

}