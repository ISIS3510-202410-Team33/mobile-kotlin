package com.example.ventura.model

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class CampusLocationsModel(private val context: Context) {
    private val localFile = File(context.filesDir, "edificios.json")

    init {
        if (!localFile.exists()) {
            copyJsonFromAssets()
        }
    }

    private fun copyJsonFromAssets() {
        try {
            context.assets.open("edificios.json").use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("CampusLocationsModel", "used assets fallback for edificios.json")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isDataAvailableLocally(): Boolean {
        return localFile.exists()
    }

    fun getFileLastModifiedDate(): String {
        if (!localFile.exists()) return "N/A"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date(localFile.lastModified()))
    }

    suspend fun updateJsonData(): Pair<JSONObject?, String> {
        try {
            val storage = Firebase.storage
            val jsonRef = storage.reference.child("edificios.json")
            val jsonBytes = jsonRef.getBytes(10 * 1024 * 1024).await()
            val jsonString = String(jsonBytes)
            val json = JSONObject(jsonString)
            FileOutputStream(localFile).use { output ->
                output.write(jsonBytes)
            }
            return Pair(json, "Data downloaded and stored locally")
        } catch (e: Exception) {
            return Pair(null, "Error downloading data: ${e.localizedMessage}")
        }
    }

    fun fetchJsonData(): Pair<JSONObject?, String> {
        if (localFile.exists()) {
            var lastModifiedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(localFile.lastModified()))
            lastModifiedDate = lastModifiedDate.substring(0, 10) + " at " + lastModifiedDate.substring(11, 16) + ". Swipe down to update."

            val jsonString = localFile.readText()
            val json = JSONObject(jsonString)
            Log.d("CampusLocationsModel", "used local file for edificios.json")

            return Pair(json, "Showing data from $lastModifiedDate")
        } else {
            return Pair(null, "No data available locally. Swipe down to update.")
        }
    }
}