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

class JsonViewModel(private val context: Context) : ViewModel() {
    private val storage = Firebase.storage
    private val jsonRef = storage.reference.child("edificios.json")
    private val localFile = File(ContextCompat.getExternalFilesDirs(context, null)[0], "edificios.json")

    fun isDataAvailableLocally(): Boolean {
        return localFile.exists()
    }

    suspend fun fetchJsonData(): JSONObject {
        return viewModelScope.async(Dispatchers.IO) {
            try {
                if (localFile.exists()) {
                    val jsonString = localFile.readText()
                    return@async JSONObject(jsonString)
                }

                val jsonBytes = jsonRef.getBytes(10 * 1024 * 1024).await()
                val jsonString = String(jsonBytes)
                val json = JSONObject(jsonString)

                FileOutputStream(localFile).use { output ->
                    output.write(jsonBytes)
                }

                json
            } catch (e: Exception) {
                throw e
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