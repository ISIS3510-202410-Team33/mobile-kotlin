package com.example.ventura.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.example.ventura.data.models.StepCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


private val TAG = "StepCounterRepository"


class StepCounterRepository(
//    private val application: Application,
    private val stepCountDao: StepCountDao
) {
    // daily step objective
    val dailyStepsObjective = 500
    // daily calories objective
    val dailyCaloriesObjective = 2300

    val stepCount: Flow<StepCount> = stepCountDao.findByDateOfMeasurement(
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    )

    init {
        // TODO: ask permissions to use 'fitness features'
        Log.d(TAG, "Created")
    }

    private suspend fun isDailyStepCountSet(): Boolean {
        Log.d(TAG, "Is daily step count set?")
        return withContext(Dispatchers.IO) {
            stepCount.firstOrNull()?.let { it.stepsAtNow > 0 } ?: false
        }
    }


    @WorkerThread
    private suspend fun insertStepCount(
        stepsAtDayStart: Int,
        stepsAtNow: Int,
        dateOfMeasurement: String
    ) {
        stepCountDao.insertStepCount(
            StepCount(
                stepsAtDayStart = stepsAtDayStart,
                stepsAtNow = stepsAtNow,
                dateOfMeasurement = dateOfMeasurement
            )
        )
        Log.d(TAG, "Inserted step count with $stepsAtNow")
    }


    @WorkerThread
    private suspend fun updateStepsAtNow(
        dateOfMeasurement: String,
        stepsAtNow: Int
    ) {
        stepCountDao.updateStepsAtNow(
            stepsAtNow = stepsAtNow,
            date = dateOfMeasurement
        )
        Log.d(TAG, "Updated step count to $stepsAtNow")
    }


    /**
     * Updates the steps for the day
     */

    @WorkerThread
    suspend fun updateSteps(steps: Int) {
        if (!isDailyStepCountSet()) {
            insertStepCount(
                stepsAtDayStart = steps,
                stepsAtNow = steps,
                dateOfMeasurement = LocalDate.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        } else {
            updateStepsAtNow(
                dateOfMeasurement = LocalDate.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE),
                stepsAtNow = steps.toInt()
            )
        }

        Log.d(TAG, "Steps updated in database")
    }


    /**
     * Returns the steps for the day.
     * If this method is reached, onCreate was reached and there is guarantee of
     * an entry in the database
     */
    suspend fun getDailySteps(): Int {
        return if (isDailyStepCountSet()) stepCount.first().stepsAtNow - stepCount.first().stepsAtDayStart
        else 0
    }

}