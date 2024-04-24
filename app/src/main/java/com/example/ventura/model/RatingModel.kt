// RatingModel.kt
package com.example.ventura.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class RatingModel {
    private val storageRef = FirebaseStorage.getInstance().reference

    fun enviarCalificacionALaBaseDeDatos(spaceKey: String, calificacion: Float, comentario: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val nuevaCalificacion = JSONArray().apply {
            put(calificacion.toInt())
            put(comentario)
        }
        actualizarCalificacionEnFirebase(spaceKey, nuevaCalificacion, resultLiveData)
        return resultLiveData
    }

    private fun actualizarCalificacionEnFirebase(spaceKey: String, nuevaCalificacion: JSONArray, resultLiveData: MutableLiveData<Boolean>) {
        val calificacionesRef = storageRef.child("calificaciones.json")
        calificacionesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val jsonStr = String(bytes, Charset.defaultCharset())
            val json = JSONObject(jsonStr)
            if (json.has("spaces")) {
                val spaces = json.getJSONObject("spaces")
                if (spaces.has(spaceKey)) {
                    val space = spaces.getJSONObject(spaceKey)
                    val calificacionesArray = space.getJSONArray("calificaciones")
                    calificacionesArray.put(nuevaCalificacion)
                } else {
                    val calificacionesArray = JSONArray().apply {
                        put(nuevaCalificacion)
                    }
                    val space = JSONObject().apply {
                        put("calificaciones", calificacionesArray)
                    }
                    spaces.put(spaceKey, space)
                }
            } else {
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
            val nuevoJsonStr = json.toString()
            val bytes = nuevoJsonStr.toByteArray(Charset.defaultCharset())
            calificacionesRef.putBytes(bytes)
                .addOnSuccessListener {
                    resultLiveData.postValue(true)
                }
                .addOnFailureListener { e ->
                    Log.e("RatingModel", "Error al enviar la calificación a Firebase: ${e.message}", e)
                    resultLiveData.postValue(false)
                }
        }.addOnFailureListener { e ->
            Log.e("RatingModel", "Error al obtener el archivo JSON de Firebase: ${e.message}", e)
            resultLiveData.postValue(false)
        }
    }

    fun obtenerEdificioConMejorPuntaje(): LiveData<String> {
        val mejorEdificioLiveData = MutableLiveData<String>()
        var mejorPuntaje = Float.MIN_VALUE
        val calificacionesRef = storageRef.child("calificaciones.json")
        calificacionesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val jsonStr = String(bytes, Charset.defaultCharset())
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
            Log.e("RatingModel", "Error al obtener el archivo JSON de Firebase: ${e.message}", e)
        }
        return mejorEdificioLiveData
    }
}