package com.example.ventura.view

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.ventura.R
import android.os.CountDownTimer
import android.widget.Toast


class NoInternetLogin: ComponentActivity() {

    private var isButtonClickable = true

    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.login_no_internet)

            val tryAgainButton = findViewById<Button>(R.id.button4)
            tryAgainButton.setOnClickListener {
                if (isButtonClickable) {
                    isButtonClickable = false
                    tryAgainButton.text = "Please wait..."
                    object : CountDownTimer(10000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            tryAgainButton.text = "Try again in..." + (millisUntilFinished / 1000).toString()
                        }

                        override fun onFinish() {
                            tryAgainButton.text = "Try Again"
                            isButtonClickable = true
                        }
                    }.start()

                    checkInternetConnectivity()
                }

            }
        } catch (e: Exception) {
                Log.d("Execution Error", e.toString())
            }
        }
    private fun checkInternetConnectivity() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        if (networkCapabilities == null ||
            (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        ) {
            // No hay conexión a Internet
            showToast("Not internet connection found...")
        } else {

            // Hay conexión a Internet, iniciar MainMenuActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    }



