package com.example.ventura.ui.activity

import androidx.activity.ComponentActivity


private const val TAG = "StepCounterActivity"


class StepCounterActivity : ComponentActivity() {

//    private val sensorManager: SensorManager by lazy {
//        getSystemService(SENSOR_SERVICE) as SensorManager
//    }
//    private val steps: Sensor? by lazy {
//        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//    }
//    private lateinit var stepCounterViewModel: StepCounterViewModel
//

//    override fun onCreate(savedInstanceState: Bundle?) {
//        // TODO: ask permissions to use 'fitness features'
//        Log.d(TAG, "Created")
//        super.onCreate(savedInstanceState)
//
//        stepCounterViewModel = ViewModelProvider(
//            this,
//            StepCounterViewModelFactory((application as PermanentSensorsApplication).stepCounterRepository)
//        )[StepCounterViewModel::class.java]
//    }
//
//
//    override fun onResume() {
//        Log.d(TAG, "Resumed")
//        super.onResume()
//        sensorManager.registerListener(this, steps, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//
//
//    override fun onPause() {
//        Log.d(TAG, "Paused")
//        super.onPause()
//        sensorManager.unregisterListener(this)
//    }
//
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        Log.d(TAG, "Sensor changed...")
//        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
//            stepCounterViewModel.updateSteps(event)
//        }
////        Log.d(TAG, "Steps: ${stepCounterViewModel.stepCount.value?.stepsAtNow}")
//    }
//
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        Log.d(TAG, "Accuracy changed...")
//        return
//    }
}