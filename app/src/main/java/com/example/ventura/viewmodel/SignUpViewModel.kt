package com.example.ventura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventura.model.SignUpModel
import kotlinx.coroutines.launch

class SignUpViewModel(private val model: SignUpModel) : ViewModel() {

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            model.signUp(email, password, onSuccess, onFailure)
        }
    }
}