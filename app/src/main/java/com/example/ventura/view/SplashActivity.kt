package com.example.ventura.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import kotlinx.coroutines.*

// <- This is the main activity defined in the manifest.xml ->
class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 4000 // 4 sec

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        CoroutineScope(Dispatchers.Main).launch {
            delay(splashTimeOut)
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
