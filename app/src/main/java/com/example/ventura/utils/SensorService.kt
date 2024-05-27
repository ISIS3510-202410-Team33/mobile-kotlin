package com.example.ventura.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ventura.PermanentSensorsApplication
import com.example.ventura.R
import com.example.ventura.model.SensorModel
import com.example.ventura.ui.activity.MainMenuActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SensorService"
private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "SensorServiceChannel"

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

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

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

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainMenuActivity::class.java) // Change to your main activity
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sensor Service")
            .setContentText("Monitoring sensors in the background")
            .setSmallIcon(R.drawable.rain)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sensor Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "Sensor changed")
        event?.sensor?.type?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    sensorModel.processSensorEvent(event)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing the sensor event", e)
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
