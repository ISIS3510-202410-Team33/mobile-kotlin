package com.example.ventura.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ventura.model.data.Theme
import com.example.ventura.model.data.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


enum class ThemeClarity


/**
 * Data class for the theme
 */
data class ThemeUiState(
    val theme: Theme = Theme("system")
)


class ThemeViewModel : ViewModel() {
    private val themeRepository = ThemeRepository()

    // inner object, modifiable by the ViewModel
    private val _uiState = MutableStateFlow(ThemeUiState())

    // returned, only viewable object for the view
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    /**
     * At start it refreshes data
     */
    init {
        refreshThemeData()
    }


    fun refreshThemeData() {
        _uiState.value = ThemeUiState(theme = themeRepository.getThemeData())
        Log.d("refresh-theme", "Current theme setting is ${_uiState.value.theme.setting}")
    }


    /**
     * Updates the theme setting
     * @param newSetting new value for the new lighting setting
     */
    fun changeThemeSetting(newSetting: String) {
        Log.d("change-theme", "Changing to $newSetting")
        _uiState.update { currentState ->
            currentState.copy(
                theme = currentState.theme.copy(setting = newSetting)
            )
        }

        themeRepository.updateThemeData(uiState.value.theme)
        Log.d("change-theme", "Changed to ${_uiState.value.theme.setting}")
    }
}