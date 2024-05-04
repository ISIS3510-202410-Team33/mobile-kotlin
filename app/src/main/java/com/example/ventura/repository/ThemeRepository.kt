package com.example.ventura.repository

import com.example.ventura.data.models.Theme

class ThemeRepository {
    private var currentTheme = Theme("light_sensitive")

    fun getThemeData(): Theme {
        // TODO: fetch theme data
        return currentTheme
    }


    fun updateThemeData(newTheme: Theme) {
        currentTheme = newTheme
        return
    }
}