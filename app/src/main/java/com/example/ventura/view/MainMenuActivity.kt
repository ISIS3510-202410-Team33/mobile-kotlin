package com.example.ventura.view

import android.os.Bundle
import com.example.ventura.R

import android.widget.Button
import androidx.activity.ComponentActivity

class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val buttonProfile = findViewById<Button>(R.id.buttonProfile)
        val buttonMap = findViewById<Button>(R.id.buttonMap)
        val buttonSettings = findViewById<Button>(R.id.buttonSettings)

        buttonProfile.setOnClickListener {
            // Navigate to Profile Activity
        }

        buttonMap.setOnClickListener {
            // Navigate to Map Activity
        }

        buttonSettings.setOnClickListener {
            // Navigate to Settings Activity
        }
    }
}