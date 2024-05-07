package com.example.ventura.data.ui

import com.example.ventura.data.models.Theme


/**
 * Data class to represent the UI state of the Theme
 */
data class ThemeUiState(
    val theme: Theme = Theme("system")
)