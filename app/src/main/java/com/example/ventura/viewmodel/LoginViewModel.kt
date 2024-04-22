package com.example.ventura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventura.model.LoginModel
import kotlinx.coroutines.launch

class LoginViewModel(private val model: LoginModel) : ViewModel() {

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            model.signIn(email, password, onSuccess, onFailure)
        }
    }
}