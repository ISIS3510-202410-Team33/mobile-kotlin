// UserPreferencesModel.kt
package com.example.ventura.model

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

class UserPreferencesModel {
    private val storageRef = FirebaseStorage.getInstance().reference

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

    suspend fun getData(context: Context, userEmail: String): JSONObject? {
        val cacheFile = File(context.cacheDir, "$userEmail.json")
        if (cacheFile.exists()) {
            val jsonString = cacheFile.readText(Charset.defaultCharset())
            Log.d("UserPreferencesModel", "Used cache to get data")
            return JSONObject(jsonString)
        }

        val localStorageFile = File(context.filesDir, "$userEmail.json")
        if (localStorageFile.exists()) {
            val jsonString = localStorageFile.readText(Charset.defaultCharset())
            val jsonObject = JSONObject(jsonString)
            cacheFile.writeText(jsonString, Charset.defaultCharset())
            Log.d("UserPreferencesModel", "Used local storage to get data")
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
            }
            .addOnFailureListener {
                jsonObject = JSONObject()
            }

            Log.d("UserPreferencesModel", "Used Firebase to get data")
        if (jsonObject != null){
            return jsonObject
        }

        Log.d("UserPreferencesModel", "Failed to get data, the user has no preferences")
        
        // returns an empty JSONObject if the user has no preferences
        return JSONObject()
    }
}