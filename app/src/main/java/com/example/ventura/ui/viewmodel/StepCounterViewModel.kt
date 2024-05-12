package com.example.ventura.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.ventura.data.models.StepCount
import com.example.ventura.data.ui.StepCountUi
import com.example.ventura.repository.StepCounterRepository


private val TAG = "StepCounterViewModel"

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

    // better uiState ?
    val stepCount: LiveData<StepCount> = stepCounterRepository.stepCount.asLiveData()


    fun getDailyStepsObjective(): Int {
        return stepCounterRepository.dailyStepsObjective
    }

    fun getDailyCaloriesObjective(): Int {
        return stepCounterRepository.dailyCaloriesObjective
    }

}