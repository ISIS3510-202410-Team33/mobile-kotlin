package com.example.ventura.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.nio.charset.Charset
import android.content.Context
import android.util.Log
import java.io.File

class UserPreferencesViewModel : ViewModel() {

    fun saveOrUpdateData(userEmail: String, buildingName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userRef = storageRef.child("$userEmail.json")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                // Si el archivo existe, actualizar los datos
                val jsonString = String(bytes, Charset.defaultCharset())
                val jsonObject = JSONObject(jsonString)
                if (jsonObject.has(buildingName)) {
                    val clicks = jsonObject.getInt(buildingName)
                    jsonObject.put(buildingName, clicks + 1)
                } else {
                    jsonObject.put(buildingName, 1)
                }
                // Guardar los datos actualizados en Firebase Storage
                userRef.putBytes(jsonObject.toString().toByteArray())
            }
            .addOnFailureListener { exception ->
                // Si el archivo no existe, crearlo con los datos iniciales
                val jsonObject = JSONObject()
                jsonObject.put(buildingName, 1)
                // Guardar los datos en Firebase Storage
                userRef.putBytes(jsonObject.toString().toByteArray())
            }
    }

    fun getData(context: Context, userEmail: String?): LiveData<JSONObject> {
        val data = MutableLiveData<JSONObject>()

        // Verificar si el archivo existe en la caché
        val cacheFile = File(context.cacheDir, "$userEmail.json")
        if (cacheFile.exists()) {
            Log.d("CACHE JUAN", "EXISTE EL ARCHIVO EN CACHÉ")
            val jsonString = cacheFile.readText(Charset.defaultCharset())
            val jsonObject = JSONObject(jsonString)
            data.value = jsonObject
            return data
        }

        // Verificar si el archivo existe en el almacenamiento local
        val localStorageFile = File(context.filesDir, "$userEmail.json")
        if (localStorageFile.exists()) {
            Log.d("CACHE JUAN 2", "NO EXISTE EL ARCHIVO EN CACHÉ, PERO EXISTE EN LOCAL STORAGE")
            val jsonString = localStorageFile.readText(Charset.defaultCharset())
            val jsonObject = JSONObject(jsonString)
            data.value = jsonObject

            // Guardar en caché
            cacheFile.writeText(jsonString, Charset.defaultCharset())

            return data
        }

        // Obtener el archivo de Firebase
        val storageRef = FirebaseStorage.getInstance().reference
        val userRef = storageRef.child("$userEmail.json")
        Log.d("CACHE JUAN 3", "NO EXISTE EL ARCHIVO, NOS VAMOS AL FIREBASE Y GUARDAMOS EN LOCAL STORAGE Y CACHE")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes, Charset.defaultCharset())
                val jsonObject = JSONObject(jsonString)
                data.value = jsonObject

                // Guardar en local storage
                localStorageFile.writeText(jsonString, Charset.defaultCharset())

                // Guardar en caché
                cacheFile.writeText(jsonString, Charset.defaultCharset())
            }
            .addOnFailureListener {
                data.value = JSONObject() // Objeto JSON vacío en caso de fallo
            }

        return data
    }
}
