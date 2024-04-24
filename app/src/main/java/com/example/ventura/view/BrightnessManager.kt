package com.example.ventura.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.ventura.model.data.Theme


/**
 * Manages and interprets the brightness sensor
 */
class BrightnessManager(
    private val onNewBrightness: (Boolean) -> Unit,
    private var sensorManager: SensorManager

): SensorEventListener {
    // max light value to be considered dark
    private val DARKUPPERBOUND = 5000

    private var sensor: Sensor? = null


    init {
        // sensor manager given by activity
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }


    private fun getTooBright(brightness: Float): Boolean {
        return when (brightness.toInt()) {
            in 0..DARKUPPERBOUND -> false
            else -> true
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]
            val newTooBright = getTooBright(light)

            onNewBrightness(newTooBright)
        }
        else Log.e("brightness-manager", "Light sensor might not be initialized")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }


    fun updateThemeSetting(newTheme: Theme) {
        // new theme requires sensor
        if (newTheme.setting == "light_sensitive") {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            sensorManager.unregisterListener(this)
        }
    }
}