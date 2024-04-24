package com.example.ventura.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.model.RatingModel

class RatingViewModel(private val model: RatingModel) : ViewModel() {

    fun enviarCalificacionALaBaseDeDatos(spaceKey: String, calificacion: Float, comentario: String): LiveData<Boolean> {
        return model.enviarCalificacionALaBaseDeDatos(spaceKey, calificacion, comentario)
    }

    fun obtenerEdificioConMejorPuntaje(): LiveData<String> {
        return model.obtenerEdificioConMejorPuntaje()
    }
}

class RatingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            val model = RatingModel()
            @Suppress("UNCHECKED_CAST")
            return RatingViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}