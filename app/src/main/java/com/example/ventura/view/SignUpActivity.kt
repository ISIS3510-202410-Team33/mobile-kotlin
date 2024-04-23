package com.example.ventura.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.ventura.R
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.SignUpViewModel
import com.example.ventura.viewmodel.SignUpViewModelFactory

class SignUpActivity : ComponentActivity() {
    private lateinit var viewModel: SignUpViewModel
    private val featureCrashHandler = FeatureCrashHandler("sign_up")

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_signup)

            val factory = SignUpViewModelFactory(this)
            viewModel = ViewModelProvider(this, factory)[SignUpViewModel::class.java]

            val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
            val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
            val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)

            buttonSignUp.setOnClickListener {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()
                val validationResult = validateInput(email, password)

                if (validationResult == "Valid") {
                    viewModel.signUp(email, password,
                        onSuccess = {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                            Toast.makeText(baseContext, "Successfully registered. Please log in.", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(this, validationResult, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            featureCrashHandler.logCrash("display", e)
        }
    }

    private fun validateInput(email: String, password: String): String {
        if (email.isEmpty() || password.isEmpty()) return "Email and password must not be empty"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email address"
        if (password.length < 7) return "Password must have at least 7 characters"
        if (!password.any { it.isDigit() }) return "Password must contain at least 1 number"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least 1 uppercase letter"
        if (!password.any { it.isLetter() }) return "Password must contain at least 1 letter"
        return "Valid"
    }
}
