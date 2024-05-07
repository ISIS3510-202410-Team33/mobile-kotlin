package com.example.ventura.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.example.ventura.repository.LightSensitiveThemeRepository
import com.example.ventura.repository.StepCounterRepository


private val TAG = "SENSOR_MODEL"


class SensorModel(
    private val stepCounterRepository: StepCounterRepository,
    private val lightSensitiveThemeRepository: LightSensitiveThemeRepository
) {

    /**
     * Processes a sensor event and stores relevant data appropriately
     * using corresponding repositories
     * @param event Non-null SensorEvent
     */
    suspend fun processSensorEvent(event: SensorEvent) {
        /*
         light measurements are only required when app is running. No need to persist
         However, saving light measurements quickly isn't essential for UI/UX. Moreover,
         light measurements might be persisted in future implementations, which would add
         immense Dispatchers.Main overhead. Thus Dispatchers.IO is appropriate
         */
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            Log.d(TAG, "Light sensor update")
            lightSensitiveThemeRepository.updateBrightness(
                light=event.values[0].toInt()
            )
        }

        /*
         step counts are meant to be persisted. These are last priority and fast or mediumly
         paced updates are not key for UI/UX. Thus, Dispatchers.Default is the appropriate level
         */
        else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "Step sensor update")
            stepCounterRepository.updateSteps(
                steps=event.values[0].toInt()
            )
        }
    }



}