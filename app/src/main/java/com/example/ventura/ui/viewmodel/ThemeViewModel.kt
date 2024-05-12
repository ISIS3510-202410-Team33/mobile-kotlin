package com.example.ventura.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.data.models.Theme
import com.example.ventura.repository.LightSensitiveThemeRepository
import com.example.ventura.repository.ThemeRepository


private val TAG = "ThemeViewModel"


class ThemeViewModelFactory(
    private val lightRepository: LightSensitiveThemeRepository,
    private val themeRepository: ThemeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(lightRepository, themeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



class ThemeViewModel(
    private val lightSensitiveThemeRepository: LightSensitiveThemeRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    // observable for when it is too bright
    val tooBright: LiveData <Boolean> = lightSensitiveThemeRepository.getBrightness()

    // observable
    private val _themeSetting = MutableLiveData(themeRepository.getThemeSetting())
    val themeSetting: LiveData <Theme> = _themeSetting


    /**
     * Updates the theme setting
     * @param newSetting new value for the new lighting setting
     */
    fun updateThemeSetting(newSetting: String) {
        themeRepository.updateThemeSetting(newSetting)
        _themeSetting.value = themeRepository.getThemeSetting()
        Log.d(TAG, "themeSetting.value = ${themeSetting.value}")
    }
}
