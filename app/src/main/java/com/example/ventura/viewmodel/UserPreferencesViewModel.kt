package com.example.ventura.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.nio.charset.Charset

class UserPreferencesViewModel : ViewModel() {

    fun saveOrUpdateData(userEmail: String, buildingName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userRef = storageRef.child("$userEmail.json")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                // If th JSON exists we just update the data
                val jsonString = String(bytes, Charset.defaultCharset())
                val jsonObject = JSONObject(jsonString)
                if (jsonObject.has(buildingName)) {
                    val clicks = jsonObject.getInt(buildingName)
                    jsonObject.put(buildingName, clicks + 1)
                } else {
                    jsonObject.put(buildingName, 1)
                }
                // Then we save the data in the firebase storage
                userRef.putBytes(jsonObject.toString().toByteArray())
            }
            .addOnFailureListener { exception ->
                // Else, if the JSON doesn´t exist yet, we create a new one
                val jsonObject = JSONObject()
                jsonObject.put(buildingName, 1)
                // Then we save the data in the firebase storage
                userRef.putBytes(jsonObject.toString().toByteArray())
            }
    }

    /* Here we get the data source with the JSON preferences of the user to be used
     * by the other app components.
     */
    fun getData(userEmail: String?): LiveData<JSONObject> {
        val data = MutableLiveData<JSONObject>()
        val storageRef = FirebaseStorage.getInstance().reference
        val userRef = storageRef.child("$userEmail.json")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes, Charset.defaultCharset())
                val jsonObject = JSONObject(jsonString)
                data.value = jsonObject
            }
            .addOnFailureListener {
                data.value = JSONObject() // Empty JSON object in case of failure
            }

        return data
    }
}
