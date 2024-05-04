package com.example.ventura.ui.activity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R
import com.example.ventura.ui.adapter.MejorEdificioAdapter
import com.example.ventura.ui.viewmodel.RatingViewModel
import kotlinx.coroutines.*
import com.example.ventura.ui.viewmodel.RatingViewModelFactory

class NotificationsActivity : AppCompatActivity() {
    private lateinit var ratingViewModel: RatingViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var mejorEdificioAdapter: MejorEdificioAdapter
    private lateinit var textViewNoConnection: TextView
    private lateinit var imageViewNoNet: ImageView
    private var connectivityJob: Job? = null // Corutina para verificar la conectividad

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.notifications_page)


            recyclerView = findViewById(R.id.notification_recycler)
            mejorEdificioAdapter = MejorEdificioAdapter(mutableListOf())
            recyclerView.adapter = mejorEdificioAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
            textViewNoConnection = findViewById(R.id.textViewNoConnection)
            imageViewNoNet = findViewById(R.id.imageView6)
            ratingViewModel = ViewModelProvider(this, RatingViewModelFactory(this)).get(
                RatingViewModel::class.java)

            ratingViewModel.obtenerEdificioConMejorPuntaje().observe(this, Observer { mejorEdificio ->
                // Here we add the notifications to the user notifications
                mejorEdificioAdapter.addMejorEdificio(mejorEdificio)
            })

            val backButton = findViewById<ImageView>(R.id.buttonBackToMenu2)
            backButton.setOnClickListener {
                finish()
            }

            // Iniciar la corutina para verificar la conectividad a Internet
            connectivityJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(1000) // Verificar cada segundo
                    val isConnected = isConnectedToNetwork()
                    withContext(Dispatchers.Main) {
                        if (!isConnected) {
                            // No hay conexión a Internet
                            textViewNoConnection.visibility = TextView.VISIBLE
                            imageViewNoNet.visibility = ImageView.VISIBLE
                        } else {
                            // Hay conexión a Internet
                            textViewNoConnection.visibility = TextView.GONE
                            imageViewNoNet.visibility = ImageView.GONE
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.d("display", e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar la corutina cuando se destruye la actividad para evitar memory leaks
        connectivityJob?.cancel()
    }

    private fun isConnectedToNetwork(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}
