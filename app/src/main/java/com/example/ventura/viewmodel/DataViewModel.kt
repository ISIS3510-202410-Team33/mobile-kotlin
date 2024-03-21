package com.example.ventura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class DataViewModel : ViewModel() {

    private var email: String = ""
    private var password: String = ""
    val loginStatus: MutableLiveData<Boolean> = MutableLiveData()

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun onLoginButtonClick() {
        // You can perform your login logic here
        // For now, we're setting loginStatus to true regardless of the login logic
        loginStatus.postValue(true)
    }
}