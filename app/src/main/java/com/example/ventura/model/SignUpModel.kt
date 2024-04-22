package com.example.ventura.model

import com.google.firebase.auth.FirebaseAuth

class SignUpModel {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }
}