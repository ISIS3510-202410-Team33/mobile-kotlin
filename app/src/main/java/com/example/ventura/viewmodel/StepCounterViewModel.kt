package com.example.ventura.viewmodel

import android.hardware.SensorEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ventura.model.data.StepCount
import com.example.ventura.model.data.StepCountUi
import com.example.ventura.repository.StepCounterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


private val TAG = "STEP_COUNTER_VIEWMODEL"

data class StepCounterUiState(
    val stepCountUi: StepCountUi = StepCountUi()
)


class StepCounterViewModelFactory(private val repository: StepCounterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepCounterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
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

    // better uiState ?
    val stepCount: LiveData<StepCount> = stepCounterRepository.stepCount.asLiveData()


    fun updateSteps(event: SensorEvent?) = viewModelScope.launch {
        stepCounterRepository.updateSteps(event)

        // TODO: _uiState should be eliminated as it breaks the observer flow
//        _uiState.update { currentState ->
//            currentState.copy(
//                stepCountUi = currentState.stepCountUi.copy(
//                    steps = stepCounterRepository.getDailySteps(),
//                    dateOfMeasurement = stepCount.value?.dateOfMeasurement!!
//                )
//            )
//        }
    }

}