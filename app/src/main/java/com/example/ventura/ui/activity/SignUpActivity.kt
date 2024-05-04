package com.example.ventura.ui.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.R
import com.example.ventura.utils.FeatureCrashHandler
import com.example.ventura.ui.viewmodel.SignUpViewModel
import com.example.ventura.ui.viewmodel.SignUpViewModelFactory
import kotlinx.coroutines.*

class SignUpActivity : ComponentActivity() {
    private lateinit var viewModel: SignUpViewModel
    private val featureCrashHandler = FeatureCrashHandler("sign_up")
    private lateinit var textViewOffline: TextView
    private var isConnected = false
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_signup)

            val factory = SignUpViewModelFactory(this)
            viewModel = ViewModelProvider(this, factory)[SignUpViewModel::class.java]

            val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
            val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
            val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)

            textViewOffline = findViewById(R.id.textViewOffline)

            job = CoroutineScope(Dispatchers.Main).launch {
                listenToNetworkChanges()
            }

            buttonSignUp.setOnClickListener {
                if (isConnected) {
                    val email = editTextEmail.text.toString()
                    val password = editTextPassword.text.toString()
                    val validationResult = validateInput(email, password)

                    if (validationResult == "Valid") {
                        viewModel.signUp(email, password,
                            onSuccess = {
                                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                                finish()
                                Toast.makeText(baseContext, "Successfully registered. Please log in.", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(this@SignUpActivity, validationResult, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "No internet connection available", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            featureCrashHandler.logCrash("display", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private suspend fun listenToNetworkChanges() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = android.net.NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isConnected = true
                CoroutineScope(Dispatchers.Main).launch {
                    textViewOffline.visibility = TextView.GONE
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isConnected = false
                CoroutineScope(Dispatchers.Main).launch {
                    textViewOffline.visibility = TextView.VISIBLE
                }
            }
        }

        withContext(Dispatchers.IO) {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
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
