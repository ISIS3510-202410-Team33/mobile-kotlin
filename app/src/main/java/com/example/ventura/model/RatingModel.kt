// RatingModel.kt
package com.example.ventura.model

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class RatingModel(private val context: Context) {
    private var storageRef: StorageReference? = null
    private val sharedPreferences = context.getSharedPreferences("RatingModel", Context.MODE_PRIVATE)

    // This function sends a rating to the database
    fun enviarCalificacionALaBaseDeDatos(spaceKey: String, calificacion: Float, comentario: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val nuevaCalificacion = JSONArray().apply {
            put(calificacion.toInt())
            put(comentario)
        }
        // Try to update the rating in Firebase
        actualizarCalificacionEnFirebase(spaceKey, nuevaCalificacion, resultLiveData)
        return resultLiveData
    }

    // This function updates the rating in Firebase
    private fun actualizarCalificacionEnFirebase(spaceKey: String, nuevaCalificacion: JSONArray, resultLiveData: MutableLiveData<Boolean>) {
        initializeFirebase()
        val calificacionesRef = storageRef!!.child("calificaciones.json")
        calificacionesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val jsonStr = String(bytes, Charset.defaultCharset())
            val json = JSONObject(jsonStr)
            updateJson(json, spaceKey, nuevaCalificacion)
            val nuevoJsonStr = json.toString()
            val bytes = nuevoJsonStr.toByteArray(Charset.defaultCharset())
            calificacionesRef.putBytes(bytes)
                .addOnSuccessListener {
                    Log.d("RatingModel", "Successfully updated Firebase")
                    resultLiveData.postValue(true)
                    sharedPreferences.edit().putString("calificaciones", nuevoJsonStr).apply()
                }
                .addOnFailureListener { e ->
                    Log.e("RatingModel", "Error al enviar la calificación a Firebase: ${e.message}", e)
                    resultLiveData.postValue(false)
                }
        }.addOnFailureListener { e ->
            Log.e("RatingModel", "Error al obtener el archivo JSON de Firebase: ${e.message}", e)
            // If Firebase is not available, try to update the local storage
            val cachedJsonStr = sharedPreferences.getString("calificaciones", null)
            if (cachedJsonStr != null) {
                val json = JSONObject(cachedJsonStr)
                updateJson(json, spaceKey, nuevaCalificacion)
                val nuevoJsonStr = json.toString()
                sharedPreferences.edit().putString("calificaciones", nuevoJsonStr).apply()
                Log.d("RatingModel", "Successfully updated local storage")
                resultLiveData.postValue(true)
            } else {
                resultLiveData.postValue(false)
            }
        }
    }

    // This function updates the JSON object with the new rating
    private fun updateJson(json: JSONObject, spaceKey: String, nuevaCalificacion: JSONArray) {
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
    }

    // This function checks if the device is connected to the internet

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    // This function initializes Firebase
    private fun initializeFirebase() {
        if (storageRef == null) {
            storageRef = FirebaseStorage.getInstance().reference
        }
    }

    // This function gets the building with the best rating
    fun obtenerEdificioConMejorPuntaje(): LiveData<String> {
        val mejorEdificioLiveData = MutableLiveData<String>()
        var mejorPuntaje = Float.MIN_VALUE

        if (isConnectedToInternet()) {
            Log.d("RatingModel", "User is online. Fetching data from Firebase.")
            initializeFirebase()
            val calificacionesRef = storageRef!!.child("calificaciones.json")
            calificacionesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val jsonStr = String(bytes, Charset.defaultCharset())
                val json = JSONObject(jsonStr)
                processJson(json, mejorPuntaje, mejorEdificioLiveData)
                sharedPreferences.edit().putString("calificaciones", jsonStr).apply()
                Log.d("RatingModel", "Data fetched from Firebase and stored in local storage.")
            }.addOnFailureListener { e ->
                Log.e("RatingModel", "Error al obtener el archivo JSON de Firebase: ${e.message}", e)
            }
        } else {
            Log.d("RatingModel", "User is offline. Fetching data from local storage.")
            val cachedJsonStr = sharedPreferences.getString("calificaciones", null)
            if (cachedJsonStr != null) {
                val json = JSONObject(cachedJsonStr)
                processJson(json, mejorPuntaje, mejorEdificioLiveData)
                Log.d("RatingModel", "Data fetched from local storage.")
            }
        }
        return mejorEdificioLiveData
    }

    // This function processes the JSON object to calculate the best rating
    private fun processJson(json: JSONObject, mejorPuntaje: Float, mejorEdificioLiveData: MutableLiveData<String>) {
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
                    mejorEdificioLiveData.postValue(spaceKey)
                }
            }
        }
    }
}