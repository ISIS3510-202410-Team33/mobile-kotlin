package com.example.ventura.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.example.ventura.viewmodel.LoginViewModel
import com.example.ventura.viewmodel.LoginViewModelFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*

class LoginActivity : ComponentActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val featureCrashHandler = FeatureCrashHandler("login")
    private lateinit var textViewOffline: TextView
    private lateinit var textViewSignUp: TextView
    private var isConnected = false
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            textViewOffline = findViewById(R.id.textViewOffline)

            textViewSignUp = findViewById<TextView>(R.id.textViewSignUp)
            textViewSignUp.text = android.text.Html.fromHtml(
                "First time using Ventura? <b>Sign up here</b>"
            )

            val factory = LoginViewModelFactory(this)
            viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
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
            val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)

            // Inicia la corutina para escuchar cambios de red
            job = CoroutineScope(Dispatchers.Main).launch {
                listenToNetworkChanges()
            }

            buttonLogin.setOnClickListener {
                if (isConnected) {
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
                } else {
                    Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show()
                }
            }

            buttonSignUp.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }

            textViewSignUp.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            featureCrashHandler.logCrash("display", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancela la corutina cuando la actividad es destruida para evitar memory leaks
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
                    textViewSignUp.visibility = TextView.VISIBLE
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isConnected = false
                CoroutineScope(Dispatchers.Main).launch {
                    textViewOffline.visibility = TextView.VISIBLE
                    textViewSignUp.visibility = TextView.GONE
                }
            }
        }

        withContext(Dispatchers.IO) {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
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
        FirebaseCrashlytics.getInstance().setUserId(email)
        finish()
    }
}
