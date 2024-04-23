package com.example.ventura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class SignUpViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            val model = SignUpModel()
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
    