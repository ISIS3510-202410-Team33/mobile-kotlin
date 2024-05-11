package com.example.ventura.utils

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import com.example.ventura.PermanentSensorsApplication
import com.example.ventura.model.SensorModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SensorService"


/**
 * Service in charge of monitoring and delegating sensor events
 * to the corresponding models
 */
class SensorService : Service(), SensorEventListener {

    private lateinit var sensorModel: SensorModel
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var stepSensor: Sensor? = null


    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // sensors are not guaranteed as permissions may be lacking
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val application = applicationContext as PermanentSensorsApplication
        sensorModel = application.sensorModel

        Log.d(TAG, "Created")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lightSensor?.let {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Light sensor registered")
        }
        stepSensor?.let {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Step sensor registered")
        }

        return START_STICKY
    }


    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "Sensor changed")
        event?.sensor?.type?.let {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    sensorModel.processSensorEvent(event)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing the sensor event")
                }
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}