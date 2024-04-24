package com.example.ventura.model.data


/**
 * Representa la configuración de tema visual que elige la persona
 * @param setting qué configuración de visualización tiene activa. Puede ser
 * 'system', 'dark', 'light', o 'light_sensitive'
 */
data class Theme(
    val setting: String
)


// TODO: this should be renamed. It's more model, which includes repository
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