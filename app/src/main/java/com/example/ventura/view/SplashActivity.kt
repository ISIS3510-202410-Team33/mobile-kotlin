package com.example.ventura.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import com.example.ventura.model.analytics.FeatureCrashHandler
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 4000 // 4 sec
    private val featureCrashHandler = FeatureCrashHandler("splash_activity")

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            supportActionBar?.hide()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_splash)

            CoroutineScope(Dispatchers.Main).launch {
                delay(splashTimeOut)

                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

                // Verificar si las credenciales existen en el almacenamiento local
                if (sharedPreferences.contains("email")) {
                    val email = sharedPreferences.getString("email", "")
                    Log.d("Cred Juan", "Las credenciales existen en el local storage," +
                            "se procede con la actualizacion del Json de firebase" +
                            "y se persiste en el local storage ya actualizado ")
                    fetchDataAndSaveToLocal(email)
                }

                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            featureCrashHandler.logCrash("display", e)
        }
    }

    private fun fetchDataAndSaveToLocal(userEmail: String?, overwrite: Boolean = true) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userRef = storageRef.child("$userEmail.json")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes, Charset.defaultCharset())
                
                // Escribir en el almacenamiento local
                val localStorageFile = File(filesDir, "$userEmail.json")
                if (overwrite || !localStorageFile.exists()) {
                    localStorageFile.writeText(jsonString, Charset.defaultCharset())
                    Log.d("Cred Juan 2", "Se actualizo el JSON con las preferencias del " +
                            "usuario en local storage!")
                }
            }
            .addOnFailureListener {
                // En caso de fallo, no hacemos nada o manejamos el error según tu lógica
            }
    }

}
