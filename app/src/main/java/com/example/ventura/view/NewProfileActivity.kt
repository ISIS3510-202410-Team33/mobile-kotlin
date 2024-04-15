package com.example.ventura.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ventura.ui.screens.ProfileScreen
import com.example.ventura.ui.theme.VenturaTheme
import com.example.ventura.viewmodel.ProfileViewModel

class NewProfileActivity : ComponentActivity() {
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            VenturaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProfileScreen()
                }
            }
        }
    }
}