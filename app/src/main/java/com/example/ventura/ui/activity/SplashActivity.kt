package com.example.ventura.ui.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import com.example.ventura.utils.FeatureCrashHandler
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

            /*
            First Juan Coroutine: < Dispatchers Main >
            We launch the coroutine scope into the Main thread provided
            by the grand central dispatcher. We use a suspend function which is
            delay to give a breach of time for the bundles to be loaded. Like this,
            we ensure that all the verifications and jobs inside
            the block are made before we go into the next activity.
             */
            CoroutineScope(Dispatchers.Main).launch {

                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

                // Verificar conectividad a Internet
                if (isConnectedToNetwork()) {
                    // Verificar si las credenciales existen en el almacenamiento local
                    if (sharedPreferences.contains("email")) {

                        /*
                        First Juan Storage strategy: <shared preferences>,
                        Context.MODE_PRIVATE ensures that the created file can only be accessed
                        by the calling application, not by other apps.
                         */
                        val email = sharedPreferences.getString("email", "")
                        Log.d("Cred Juan", "Las credenciales existen en el local storage," +
                                "se procede con la actualizacion del Json de firebase" +
                                "y se persiste en el local storage ya actualizado ")
                        fetchDataAndSaveToLocal(email)

                    }
                    delay(splashTimeOut)
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // Mostrar notificación emergente sobre la falta de conexión a Internet
                    delay(splashTimeOut)
                    Toast.makeText(
                        this@SplashActivity,
                        "No internet connection, preferences may not be up to date.",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this@SplashActivity, NoInternetLogin::class.java)
                    startActivity(intent)
                    finish()

                }


            }
        } catch (e: Exception) {
            featureCrashHandler.logCrash("display", e)
        }
    }

    private fun isConnectedToNetwork(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun fetchDataAndSaveToLocal(userEmail: String?, overwrite: Boolean = true) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userRef = storageRef.child("$userEmail.json")

        userRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes, Charset.defaultCharset())

                /*
                Second Juan Storage Strategy: <localStorageFile>
                We are writing directly to the disk for the private app files.
                With filesDir we are explicitly saying that the parent for our file
                is the absolute path of the directory holding the app files. Our child
                for this parent, is the file with the name "userEmail.json" where we
                storage or overwrite the user preferences according to our backend
                Ventura where we storage the most visited sites, the recommended ones,
                etc.
                 */

                val localStorageFile = File(filesDir, "$userEmail.json")
                if (overwrite || !localStorageFile.exists()) {
                    localStorageFile.writeText(jsonString, Charset.defaultCharset())
                    Log.d("Cred Juan 2", "Se actualizo el JSON con las preferencias del " +
                            "usuario en local storage!")
                }
            }
            .addOnFailureListener {
                //TODO
            }
    }

}
