package com.example.ventura.model

import com.google.firebase.auth.FirebaseAuth

class LoginModel {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }
}