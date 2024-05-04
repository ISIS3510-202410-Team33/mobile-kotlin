package com.example.ventura.data.models

/**
 * Representa la configuración de tema visual que elige la persona
 * @param setting qué configuración de visualización tiene activa. Puede ser
 * 'system', 'dark', 'light', o 'light_sensitive'
 */
data class Theme(
    val setting: String
)