package com.example.ventura.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class JsonViewModel(private val context: Context) : ViewModel() {
    private val storage = Firebase.storage
    private val jsonRef = storage.reference.child("edificios.json")
    private val localFile = File(ContextCompat.getExternalFilesDirs(context, null)[0], "edificios.json")

    fun isDataAvailableLocally(): Boolean {
        return localFile.exists()
    }

    fun getFileLastModifiedDate(): String {
        if (!localFile.exists()) return "N/A"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date(localFile.lastModified()))
    }

    suspend fun updateJsonData() {
        try {
            val jsonBytes = jsonRef.getBytes(10 * 1024 * 1024).await()
            val jsonString = String(jsonBytes)
            val json = JSONObject(jsonString)

            FileOutputStream(localFile).use { output ->
                output.write(jsonBytes)
            }
            Pair(json, "Data downloaded and stored locally")
        } catch (e: Exception) {
            Pair(null, "Error downloading data: ${e.localizedMessage}")
        }
    }

    suspend fun fetchJsonData(): Pair<JSONObject?, String> {
        return viewModelScope.async(Dispatchers.IO) {
            if (localFile.exists()) {
                var lastModifiedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(localFile.lastModified()))
                // makes the date more readable, removing ss and adding "at" before the time
                lastModifiedDate = lastModifiedDate.substring(0, 10) + " at " + lastModifiedDate.substring(11, 16) + ". Swipe down to update."


                val jsonString = localFile.readText()
                val json = JSONObject(jsonString)
                Pair(json, "Showing location data from $lastModifiedDate")
            } else {
                Pair(null, "No data available locally. Swipe down to update.")
                
            }
        }.await()
    }
}

class JsonViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JsonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JsonViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}