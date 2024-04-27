package com.example.ventura.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.StepCounterApplication
import com.example.ventura.ui.screens.ProfileScreen
import com.example.ventura.ui.theme.ThemeScreen
import com.example.ventura.viewmodel.ProfileViewModel
import com.example.ventura.viewmodel.ProfileViewModelFactory
import com.example.ventura.viewmodel.StepCounterViewModel
import com.example.ventura.viewmodel.StepCounterViewModelFactory


private val TAG = "PROFILE_ACTIVITY"


class NewProfileActivity : LightSensitiveThemeActivity(), SensorEventListener {

    private val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private val steps: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }
    private lateinit var stepCounterViewModel: StepCounterViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val profileViewModel: ProfileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(application)
        )[ProfileViewModel::class.java]

        stepCounterViewModel = ViewModelProvider(
            this,
            StepCounterViewModelFactory((application as StepCounterApplication).repository)
        )[StepCounterViewModel::class.java]

        stepCounterViewModel.stepCount.observe(this) { stepCount ->
            setContent {
                ThemeScreen(themeViewModel = themeViewModel) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        color = Color.Transparent
                    ) {
                        ProfileScreen(
                            profileViewModel = profileViewModel,
                            themeViewModel = themeViewModel,
                            stepCount = stepCount,
                            dailyStepsObjective = stepCounterViewModel.getDailyStepsObjective(),
                            dailyCaloriesObjective = stepCounterViewModel.getDailyCaloriesObjective(),
                            backToMainMenu = { finish() }
                        )
                    }
                }
            }
        }
    }


    override fun onResume() {
        Log.d(TAG, "Resumed")
        super.onResume()
        sensorManager.registerListener(this, steps, SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun onPause() {
        Log.d(TAG, "Paused")
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "Sensor changed...")
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCounterViewModel.updateSteps(event)
        }
//        Log.d(TAG, "Steps: ${stepCounterViewModel.stepCount.value?.stepsAtNow}")
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed...")
        return
    }
}



//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreviewLight() {
//    ThemeScreen(darkTheme=false) {
//        ProfileScreen { }
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreviewDark() {
//    ThemeScreen(darkTheme = true) {
//        ProfileScreen { }
//    }
//}