package com.example.ventura.repository

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.ventura.data.models.Theme


private val TAG = "ThemeRepository"


class ThemeRepository(
    application: Application
) {

    // SharedPreferences to store the light theme
    private val settingsSharedPreferences = application.getSharedPreferences("settings",
        Context.MODE_PRIVATE
    )

    /**
     * Updates the current theme setting
     * @param newTheme new theme setting to be stored
     */
    fun updateThemeSetting(newThemeSetting: String) {
        with (settingsSharedPreferences.edit()) {
            putString("themeSetting", newThemeSetting)
            commit()
        }
        Log.d(TAG, "themeSetting -> $newThemeSetting")
    }


    /**
     * Obtains the current theme setting
     * @return Theme with the current settings
     */
    fun getThemeSetting(): Theme {
        Log.d(TAG, "Getting theme setting")
        return Theme(
            settingsSharedPreferences.getString("themeSetting", "system")!!
        )
    }
}