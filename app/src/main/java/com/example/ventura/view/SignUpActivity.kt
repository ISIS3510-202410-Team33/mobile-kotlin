package com.example.ventura.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.ventura.R
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.viewmodel.SignUpViewModel

class SignUpActivity : ComponentActivity() {
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)

        buttonSignUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.signUp(email, password,
                    onSuccess = {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        Toast.makeText(baseContext, "Successfully registered. Please log in.",
                            Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}