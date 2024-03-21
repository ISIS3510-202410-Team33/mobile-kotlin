package com.example.ventura.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class JsonViewModel : ViewModel() {
    private val storage = Firebase.storage
    private val jsonRef = storage.reference.child("edificios.json")

    suspend fun fetchJsonData(): JSONObject {
        return viewModelScope.async(Dispatchers.IO) {
            try {
                val jsonBytes = jsonRef.getBytes(10 * 1024 * 1024).await()
                val jsonString = String(jsonBytes)
                JSONObject(jsonString)
            } catch (e: Exception) {
                throw e
            }
        }.await() // Esperar a que la coroutine termine y devolver el resultado
    }
}
