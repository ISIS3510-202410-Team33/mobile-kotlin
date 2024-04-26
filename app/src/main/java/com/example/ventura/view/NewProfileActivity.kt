package com.example.ventura.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.ui.screens.ProfileScreen
import com.example.ventura.ui.theme.ThemeScreen
import com.example.ventura.viewmodel.ProfileViewModel
import com.example.ventura.viewmodel.ProfileViewModelFactory

class NewProfileActivity : LightSensitiveThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val profileViewModel: ProfileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(application)
        )[ProfileViewModel::class.java]

        setContent {
            ThemeScreen(themeViewModel = themeViewModel) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    color = Color.Transparent
                ) {
                    ProfileScreen(
                        profileViewModel = profileViewModel,
                        themeViewModel = themeViewModel,
                        backToMainMenu = { finish() }
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewLight() {
    ThemeScreen(darkTheme=false) {
        ProfileScreen { }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewDark() {
    ThemeScreen(darkTheme = true) {
        ProfileScreen { }
    }
}