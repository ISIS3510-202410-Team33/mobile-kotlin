package com.example.ventura.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.ventura.R
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.LoginViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys

class LoginActivity : ComponentActivity() {
    private lateinit var viewModel: LoginViewModel
    private val featureCrashHandler = FeatureCrashHandler("login");

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

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
                            val intent = Intent(this, MainMenuActivity::class.java)
                            intent.putExtra(
                                "user_email",
                                email
                            ); // Aquí pasamos el correo como un extra
                            startActivity(intent);
                            // sets user ID for Crashlytics
                            FirebaseCrashlytics.getInstance().setUserId(email);
                            finish()
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
        } catch (e: Exception) { featureCrashHandler.logCrash("display", e); }
    }
}