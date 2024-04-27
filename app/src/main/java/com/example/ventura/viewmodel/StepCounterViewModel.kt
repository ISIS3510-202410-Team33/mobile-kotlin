package com.example.ventura.viewmodel

import android.app.Application
import android.hardware.SensorEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.model.data.StepCount
import com.example.ventura.repository.StepCounterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


private val TAG = "STEP_COUNTER_VIEWMODEL"

data class StepCounterUiState(
    val stepCount: StepCount = StepCount()
)


class StepCounterViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepCounterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val repository = StepCounterRepository(application)
            return StepCounterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class StepCounterViewModel(
    private val stepCounterRepository: StepCounterRepository
) : ViewModel() {

    // inner object, modifiable by the ViewModel
    private val _uiState = MutableStateFlow(StepCounterUiState())

    // returned, only viewable object for the view
    val uiState: StateFlow<StepCounterUiState> = _uiState.asStateFlow()


    fun updateSteps(event: SensorEvent?) {
        stepCounterRepository.updateSteps(event)
        val sc = stepCounterRepository.getStepCount()

        _uiState.update { currentState ->
            currentState.copy(
                stepCount = currentState.stepCount.copy(
                    steps = sc.steps,
                    dateOfMeasurement = sc.dateOfMeasurement
                )
            )
        }
    }

}