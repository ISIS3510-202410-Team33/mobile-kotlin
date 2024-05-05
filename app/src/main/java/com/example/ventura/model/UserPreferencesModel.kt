// UserPreferencesModel.kt
package com.example.ventura.model

import android.content.Context
import android.util.LruCache
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

class UserPreferencesModel {
    private val storageRef = FirebaseStorage.getInstance().reference

    /*
    Second Juan Cache strategy: <LruCache>
     */
    // LruCache para almacenar objetos JSONObject en memoria
    private val memoryCache = LruCache<String, JSONObject>(10) // Tamaño máximo de 10 objetos en caché

    suspend fun saveOrUpdateData(userEmail: String, buildingName: String) {
        val userRef = storageRef.child("$userEmail.json")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes, Charset.defaultCharset())
                val jsonObject = JSONObject(jsonString)
                if (jsonObject.has(buildingName)) {
                    val clicks = jsonObject.getInt(buildingName)
                    jsonObject.put(buildingName, clicks + 1)
                } else {
                    jsonObject.put(buildingName, 1)
                }
                userRef.putBytes(jsonObject.toString().toByteArray())
            }
            .addOnFailureListener {
                val jsonObject = JSONObject()
                jsonObject.put(buildingName, 1)
                userRef.putBytes(jsonObject.toString().toByteArray())
            }
    }

    /*
    First Juan Cache: <cacheFile>
     */

    suspend fun getData(context:Context, userEmail: String): JSONObject? {
        // Verificar si el objeto JSONObject está en la memoria caché
        val cachedData = memoryCache.get(userEmail)
        if (cachedData != null) {
            Log.d("UserPreferencesModel", "Used memory cache to get data")
            return cachedData
        }

        val cacheFile = File(context.cacheDir, "$userEmail.json")
        if (cacheFile.exists()) {
            val jsonString = cacheFile.readText(Charset.defaultCharset())
            val jsonObject = JSONObject(jsonString)
            Log.d("UserPreferencesModel", "Used cache to get data")
            memoryCache.put(userEmail, jsonObject) // Almacenar en caché en memoria
            return jsonObject
        }

        val localStorageFile = File(context.filesDir, "$userEmail.json")
        if (localStorageFile.exists()) {
            val jsonString = localStorageFile.readText(Charset.defaultCharset())
            val jsonObject = JSONObject(jsonString)
            cacheFile.writeText(jsonString, Charset.defaultCharset())
            Log.d("UserPreferencesModel", "Used local storage to get data")
            memoryCache.put(userEmail, jsonObject) // Almacenar en caché en memoria
            return jsonObject
        }

        val userRef = storageRef.child("$userEmail.json")
        var jsonObject: JSONObject? = null
        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes, Charset.defaultCharset())
                jsonObject = JSONObject(jsonString)
                localStorageFile.writeText(jsonString, Charset.defaultCharset())
                cacheFile.writeText(jsonString, Charset.defaultCharset())
                memoryCache.put(userEmail, jsonObject) // Almacenar en caché en memoria
            }
            .addOnFailureListener {
                jsonObject = JSONObject()
            }

        Log.d("UserPreferencesModel", "Used Firebase to get data")
        return jsonObject ?: run {
            Log.d("UserPreferencesModel", "Failed to get data, the user has no preferences")
            JSONObject()
        }
    }
}
