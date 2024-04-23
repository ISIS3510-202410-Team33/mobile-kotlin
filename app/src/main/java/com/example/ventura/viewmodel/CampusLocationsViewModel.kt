package com.example.ventura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ventura.model.CampusLocationsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class CampusLocationsViewModel(private val model: CampusLocationsModel) : ViewModel() {

    fun isDataAvailableLocally(): Boolean {
        return model.isDataAvailableLocally()
    }

    fun getFileLastModifiedDate(): String {
        return model.getFileLastModifiedDate()
    }

    suspend fun updateJsonData(): Pair<JSONObject?, String> {
        return viewModelScope.async(Dispatchers.IO) {
            try {
                val result = model.updateJsonData()
                Pair(result.first, result.second)
            } catch (e: Exception) {
                Pair(null, "Error updating data: ${e.localizedMessage}")
            }
        }.await()
    }

    suspend fun fetchJsonData(): Pair<JSONObject?, String> {
        return viewModelScope.async(Dispatchers.IO) {
            try {
                val result = model.fetchJsonData()
                Pair(result.first, result.second)
            } catch (e: Exception) {
                Pair(null, "Error fetching data: ${e.localizedMessage}")
            }
        }.await()
    }
}

class CampusLocationsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampusLocationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampusLocationsViewModel(CampusLocationsModel(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}