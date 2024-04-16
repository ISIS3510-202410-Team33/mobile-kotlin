package com.example.ventura.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewLight() {
    VenturaTheme(darkTheme=false) {
        ProfileScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewDark() {
    VenturaTheme(darkTheme = true) {
        ProfileScreen()
    }
}