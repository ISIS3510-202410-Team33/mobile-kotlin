package com.example.ventura

import android.app.Application
import android.content.Intent
import android.util.Log
import com.example.ventura.model.SensorModel
import com.example.ventura.repository.LightSensitiveThemeRepository
import com.example.ventura.repository.StepCountDatabase
import com.example.ventura.repository.StepCounterRepository
import com.example.ventura.repository.ThemeRepository
import com.example.ventura.utils.NetworkHandler
import com.example.ventura.utils.SensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

private val TAG = "PermanentSensorsApplication"

class PermanentSensorsApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    // step counting
    private val stepCountDatabase by lazy { StepCountDatabase.getDatabase(this, applicationScope) }
    val stepCounterRepository by lazy { StepCounterRepository(stepCountDatabase.stepCountDao()) }

    // light sensor
    val themeRepository by lazy { ThemeRepository(this) }
    val lightRepository by lazy { LightSensitiveThemeRepository() }

    val sensorModel by lazy { SensorModel(stepCounterRepository, lightRepository) }

    private val sensorServiceIntent by lazy { Intent(this, SensorService::class.java) }

    // network utility
    // TODO: change visibility to prevent problems
    lateinit var networkHandler: NetworkHandler

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Created")

        // starts necessary services
        startForegroundService(sensorServiceIntent)

        // creates network handler
        networkHandler = NetworkHandler(this)
        // TODO: manage crashes
    }

    override fun onTerminate() {
        super.onTerminate()

        stopService(sensorServiceIntent)
    }
}
