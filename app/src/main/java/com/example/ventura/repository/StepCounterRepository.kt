package com.example.ventura.repository

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.example.ventura.model.data.StepCount
import java.time.LocalDate


private val TAG = "STEP_COUNTER_REPOSITORY"


class StepCounterRepository(
    private val application: Application
) {
    private var todayStepsStart: Int = -1
    private var todayStepsNow: Int = -1
    private lateinit var dateOfMeasurement: LocalDate

    init {
        // TODO: ask permissions to use 'fitness features'
        Log.d(TAG, "Created")
        todayStepsStart = -1
        todayStepsNow = -1
    }


    /**
     * Validates that the date step count is set
     */
    fun isDailyStepCountSet(): Boolean {
        return todayStepsStart != -1
    }


    /**
     * Updates the steps for the day
     */
    fun updateSteps(event: SensorEvent?) {
        // verify that corresponds to step counting event
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            // no steps have been recorded for the day
            if (!isDailyStepCountSet()) todayStepsStart = event.values[0].toInt()
            // steps already recorded
            todayStepsNow = event.values[0].toInt()
            dateOfMeasurement = LocalDate.now()

            Log.d(TAG, "$todayStepsNow - $todayStepsStart = ${todayStepsNow - todayStepsStart}")
        }
    }


    /**
     * Returns the steps for the day.
     * If this method is reached, onCreate was reached and there is guarantee of
     * an entry in the database
     */
    fun getDailySteps(): Int {
        return if (isDailyStepCountSet()) todayStepsNow - todayStepsStart
        else 0
    }


    fun getStepCount(): StepCount {
        return StepCount(
            steps = getDailySteps(),
            dateOfMeasurement = dateOfMeasurement
        )
    }
}