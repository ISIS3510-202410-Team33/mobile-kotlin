package com.example.ventura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val model = LoginModel()
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}