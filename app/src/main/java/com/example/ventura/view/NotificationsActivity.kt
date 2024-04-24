package com.example.ventura.view

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R
import com.example.ventura.viewmodel.RatingViewModel
import com.example.ventura.viewmodel.RatingViewModelFactory

class NotificationsActivity: ComponentActivity() {
    private lateinit var ratingViewModel: RatingViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var mejorEdificioAdapter: MejorEdificioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.notifications_page)

            recyclerView = findViewById(R.id.notification_recycler)
            mejorEdificioAdapter = MejorEdificioAdapter(mutableListOf())
            recyclerView.adapter = mejorEdificioAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            ratingViewModel = ViewModelProvider(this, RatingViewModelFactory()).get(RatingViewModel::class.java)

            ratingViewModel.obtenerEdificioConMejorPuntaje().observe(this, Observer { mejorEdificio ->

                // Here we add the notifications to the user notifications
                mejorEdificioAdapter.addMejorEdificio(mejorEdificio)

            })

            val backButton = findViewById<ImageView>(R.id.buttonBackToMenu2)
            backButton.setOnClickListener{
                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
                finish()
            }

            // Inflar el layout que contiene el botón
            val mejorEdificioLayout = layoutInflater.inflate(R.layout.item_mejor_edificio, null)

            // Encontrar el botón dentro del layout inflado
            val visitButton = mejorEdificioLayout.findViewById<Button>(R.id.visitButton)

            visitButton.setOnClickListener{
                // Definir el enlace que deseas abrir en Google
                val url = "https://uniandes.edu.co/es/noticias-uniandes"

                // Crear un intent con la acción ACTION_VIEW y la URL del enlace
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                // Verificar que haya una aplicación que pueda manejar este intent
                if (intent.resolveActivity(packageManager) != null) {
                    // Si hay una aplicación que puede manejar el intent, iniciarla
                    startActivity(intent)
                } else {
                    // Si no hay ninguna aplicación que pueda manejar el intent, mostrar un mensaje de error o alternativa

                    Toast.makeText(this, "No se puede abrir el enlace", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.d("display", e.toString())
        }




    }
}