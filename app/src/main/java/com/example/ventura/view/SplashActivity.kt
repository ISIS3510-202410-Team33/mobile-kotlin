package com.example.ventura.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import com.example.ventura.model.analytics.FeatureCrashHandler
import kotlinx.coroutines.*

// <- This is the main activity defined in the manifest.xml ->
class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 4000 // 4 sec
    private val featureCrashHandler = FeatureCrashHandler("splash_activity");

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            supportActionBar?.hide()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_splash)

            CoroutineScope(Dispatchers.Main).launch {
                delay(splashTimeOut)
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) { featureCrashHandler.logCrash("display", e); }
    }
}
