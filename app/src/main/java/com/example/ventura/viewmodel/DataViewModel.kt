package com.example.ventura.viewmodel

import androidx.lifecycle.ViewModel

class DataViewModel : ViewModel() {

    private var email: String = ""
    private var password: String = ""

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun onLoginButtonClick() {
        // Aquí puedes implementar la lógica para manejar el evento de clic del botón de inicio de sesión
        // Por ejemplo, puedes realizar la autenticación del usuario, llamar a una función en el modelo, etc.
    }
}