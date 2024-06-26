package com.example.ventura.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R

class MejorEdificioAdapter(private val mejorEdificioList: MutableList<String>) :
    RecyclerView.Adapter<MejorEdificioAdapter.MejorEdificioViewHolder>() {

        private lateinit var url: String

    class MejorEdificioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreEdificioTextView: TextView = itemView.findViewById(R.id.nombreEdificioTextView)
        val notificationDescription: TextView = itemView.findViewById(R.id.textView14)
        val visitButton: Button =  itemView.findViewById(R.id.visitButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MejorEdificioViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mejor_edificio, parent, false)
        return MejorEdificioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MejorEdificioViewHolder, position: Int) {
        val mejorEdificio = mejorEdificioList[position]
        holder.nombreEdificioTextView.text = mejorEdificio
        holder.notificationDescription.text = "See the latest news of this building!"

        holder.visitButton.setOnClickListener {
            val context = holder.itemView.context

            url = when {
                "Japan Center" in mejorEdificio -> "https://centrodeljapon.uniandes.edu.co/"
                "W building" in mejorEdificio -> "https://campusinfo.uniandes.edu.co/es/recursos/edificios/bloquew"
                "ML Building" in mejorEdificio -> "https://campusinfo.uniandes.edu.co/es/recursos/edificios/bloqueml"
                "ML Building" in mejorEdificio -> "https://campusinfo.uniandes.edu.co/es/recursos/edificios/bloquew"
                "RGD Building" in mejorEdificio -> "https://uniandes.edu.co/es/noticias/arquitectura-y-diseno/centro-civico-universitario-un-gran-espacio-pedagogico"
                "SD Building" in mejorEdificio -> "https://campusinfo.uniandes.edu.co/es/recursos/edificios/bloquesd"
                else -> "https://uniandes.edu.co/"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(context.packageManager) == null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(holder.itemView.context, "You have no browser to open links...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return mejorEdificioList.size
    }

    fun addMejorEdificio(nombreEdificio: String) {
        mejorEdificioList.add(nombreEdificio)
        notifyItemInserted(mejorEdificioList.size - 1)
    }
}
