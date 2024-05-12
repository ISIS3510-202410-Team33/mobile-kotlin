package com.example.ventura.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private val TAG = "LightSensitiveThemeRepository"


/**
 * Manages all data related to light sensitive theme, both in
 * current light setting and current light measurements
 */
class LightSensitiveThemeRepository {
    /*
     current light conditions are kept in memory, as there is no need to persist
     It must be binded to ViewModel in order for View to update along with sensor data
     Private, modifiable LiveData object for respository, and public, unmodifiable LiveData object
     for exposure.
     */
    private val tooBright = MutableLiveData <Boolean>()

    // upper bound of dark enough
    private val DARK_UPPER_BOUND = 500


    /**
     * Returns True if light conditions are appropiate for
     * light theme
     */
    private fun isTooBright(light: Int): Boolean {
        return light !in 0..DARK_UPPER_BOUND
    }


    /**
     * Updates the light measurement in memory
     * @param light : recent LUX measurement
     */
    fun updateBrightness(light: Int) {
        Log.d(TAG, "Light: $light")
        if (tooBright.value != isTooBright(light)) {
            tooBright.postValue(isTooBright(light))
        }
    }


    /**
     * Method to retrieve the light value
     */
    fun getBrightness(): LiveData<Boolean> {
        return tooBright
    }




}