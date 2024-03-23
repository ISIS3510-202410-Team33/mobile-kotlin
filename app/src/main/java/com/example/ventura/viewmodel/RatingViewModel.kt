package com.example.ventura.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class RatingViewModel : ViewModel() {

    // Referencia a Firebase Storage
    private val storageRef = FirebaseStorage.getInstance().reference

    fun enviarCalificacionALaBaseDeDatos(spaceKey: String, calificacion: Float, comentario: String) {
        // Crear el nuevo objeto de calificación
        val nuevaCalificacion = JSONArray().apply {
            put(calificacion.toInt()) // Convertir la calificación a entero
            put(comentario)
        }

        // Actualizar el JSON en Firebase
        actualizarCalificacionEnFirebase(spaceKey, nuevaCalificacion)
    }

    private fun actualizarCalificacionEnFirebase(spaceKey: String, nuevaCalificacion: JSONArray) {
        // Obtener referencia al archivo JSON en Firebase
        val calificacionesRef = storageRef.child("calificaciones.json")

        // Leer el contenido actual del archivo JSON
        calificacionesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // Decodificar los bytes a String
            val jsonStr = String(bytes, Charset.defaultCharset())

            // Convertir el String JSON a un JSONObject
            val json = JSONObject(jsonStr)

            // Verificar si el espacio ya existe en el JSON
            if (json.has("spaces")) {
                val spaces = json.getJSONObject("spaces")

                // Verificar si el espacio ya tiene calificaciones
                if (spaces.has(spaceKey)) {
                    val space = spaces.getJSONObject(spaceKey)
                    val calificacionesArray = space.getJSONArray("calificaciones")
                    calificacionesArray.put(nuevaCalificacion)
                } else {
                    // Si el espacio no tiene calificaciones, crear un nuevo objeto de espacio y asignar las calificaciones
                    val calificacionesArray = JSONArray().apply {
                        put(nuevaCalificacion)
                    }
                    val space = JSONObject().apply {
                        put("calificaciones", calificacionesArray)
                    }
                    spaces.put(spaceKey, space)
                }
            } else {
                // Si no hay espacios en el JSON, crear uno nuevo con las calificaciones
                val calificacionesArray = JSONArray().apply {
                    put(nuevaCalificacion)
                }
                val spaces = JSONObject().apply {
                    val space = JSONObject().apply {
                        put("calificaciones", calificacionesArray)
                    }
                    put(spaceKey, space)
                }
                json.put("spaces", spaces)
            }

            // Convertir el JSONObject actualizado a String
            val nuevoJsonStr = json.toString()

            // Sobrescribir el archivo JSON en Firebase con el nuevo JSON
            val bytes = nuevoJsonStr.toByteArray(Charset.defaultCharset())
            calificacionesRef.putBytes(bytes)
                .addOnSuccessListener {
                    Log.d("RatingViewModel", "Calificación enviada exitosamente a Firebase.")
                }
                .addOnFailureListener { e ->
                    Log.e("RatingViewModel", "Error al enviar la calificación a Firebase: ${e.message}", e)
                }
        }.addOnFailureListener { e ->
            Log.e("RatingViewModel", "Error al obtener el archivo JSON de Firebase: ${e.message}", e)
        }
    }

    fun obtenerEdificioConMejorPuntaje(): LiveData<String> {
        val mejorEdificioLiveData = MutableLiveData<String>()
        var mejorPuntaje = Float.MIN_VALUE

        // Obtener referencia al archivo JSON en Firebase
        val calificacionesRef = storageRef.child("calificaciones.json")

        // Leer el contenido actual del archivo JSON
        calificacionesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // Decodificar los bytes a String
            val jsonStr = String(bytes, Charset.defaultCharset())

            // Convertir el String JSON a un JSONObject
            val json = JSONObject(jsonStr)

            if (json.has("spaces")) {
                val spaces = json.getJSONObject("spaces")
                val iterator = spaces.keys()
                while (iterator.hasNext()) {
                    val spaceKey = iterator.next()
                    val space = spaces.getJSONObject(spaceKey)
                    val calificacionesArray = space.getJSONArray("calificaciones")

                    var sum = 0F
                    var count = 0
                    for (i in 0 until calificacionesArray.length()) {
                        val calificacion = calificacionesArray.getJSONArray(i).getInt(0).toFloat()
                        sum += calificacion
                        count++
                    }
                    val promedio = if (count > 0) sum / count else 0F

                    if (promedio > mejorPuntaje) {
                        mejorPuntaje = promedio
                        mejorEdificioLiveData.postValue(spaceKey)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("RatingViewModel", "Error al obtener el archivo JSON de Firebase: ${e.message}", e)
        }

        return mejorEdificioLiveData
    }


}
