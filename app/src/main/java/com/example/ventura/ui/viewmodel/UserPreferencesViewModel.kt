package com.example.ventura.ui.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ventura.model.UserPreferencesModel
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserPreferencesViewModel(private val model: UserPreferencesModel) : ViewModel() {
    val data = MutableLiveData<JSONObject>()

    fun saveOrUpdateData(userEmail: String, buildingName: String) {
        viewModelScope.launch {
            model.saveOrUpdateData(userEmail, buildingName)
        }
    }

    fun getData(context: Context, userEmail: String) {
        viewModelScope.launch {
            val jsonObject = model.getData(context, userEmail)
            data.postValue(jsonObject!!)
        }
    }
}

class UserPreferencesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserPreferencesViewModel::class.java)) {
            val model = UserPreferencesModel()
            @Suppress("UNCHECKED_CAST")
            return UserPreferencesViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}