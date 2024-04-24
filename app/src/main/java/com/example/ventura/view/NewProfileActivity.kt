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
import com.example.ventura.ui.theme.ThemeScreen
import com.example.ventura.viewmodel.ProfileViewModel
import com.example.ventura.viewmodel.ThemeViewModel

class NewProfileActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()
    private val themeViewModel : ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ThemeScreen(themeViewModel = themeViewModel) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProfileScreen(profileViewModel, themeViewModel = themeViewModel)
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewLight() {
    ThemeScreen(darkTheme=false) {
        ProfileScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewDark() {
    ThemeScreen(darkTheme = true) {
        ProfileScreen()
    }
}