package com.example.ventura

import android.app.Application
import android.content.Intent
import android.util.Log
import com.example.ventura.model.SensorModel
import com.example.ventura.repository.LightSensitiveThemeRepository
import com.example.ventura.repository.StepCountDatabase
import com.example.ventura.repository.StepCounterRepository
import com.example.ventura.repository.ThemeRepository
import com.example.ventura.utils.SensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


private val TAG = "PERMANENT_SENSORS_APP"


class PermanentSensorsApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    // step counting
    private val stepCountDatabase by lazy { StepCountDatabase.getDatabase(this, applicationScope) }
    val stepCounterRepository by lazy { StepCounterRepository(stepCountDatabase.stepCountDao()) }

    // light sensor
    val themeRepository by lazy { ThemeRepository(this) }
    val lightRepository by lazy { LightSensitiveThemeRepository() }

    val sensorModel by lazy { SensorModel(stepCounterRepository, lightRepository) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Created")

        // starts the sensor service
        val serviceIntent = Intent(this, SensorService::class.java)
        startService(serviceIntent)
        // TODO: manage crashes
    }
}

