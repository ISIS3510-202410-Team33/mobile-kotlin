package com.example.ventura.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.R
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.LoginViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics

class LoginActivity : ComponentActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val featureCrashHandler = FeatureCrashHandler("login")

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

            if (isLoggedIn()) {
                val email = sharedPreferences.getString("email", "")
                showLoggedInMessage(email!!)
                goToMainMenu(email)
                return
            }

            val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
            val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
            val buttonLogin = findViewById<Button>(R.id.buttonLogin)
            val textViewSignUp = findViewById<TextView>(R.id.textViewSignUp)

            buttonLogin.setOnClickListener {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.signIn(email, password,
                        onSuccess = {
                            saveCredentials(email)
                            showLoggedInMessage(email)
                            goToMainMenu(email)
                        },
                        onFailure = {
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            textViewSignUp.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            featureCrashHandler.logCrash("display", e)
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.contains("email")
    }

    private fun saveCredentials(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.apply()
    }

    private fun showLoggedInMessage(email: String) {
        Toast.makeText(
            baseContext, "Successfully logged in as $email",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun goToMainMenu(email: String) {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.putExtra("user_email", email)
        startActivity(intent)
        // sets user ID for Crashlytics
        FirebaseCrashlytics.getInstance().setUserId(email)
        finish()
    }
}


