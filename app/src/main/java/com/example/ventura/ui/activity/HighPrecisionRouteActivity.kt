package com.example.ventura.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.model.HighPrecisionRouteModel
import com.example.ventura.ui.screen.HighPrecisionRouteScreen
import com.example.ventura.ui.theme.ThemeScreen
import com.example.ventura.ui.viewmodel.HighPrecisionRouteViewModel
import com.example.ventura.ui.viewmodel.HighPrecisionRouteViewModelFactory


private val TAG = "HighPrecisionRouteActivity"


/**
 * Activity that uses the high precision mapping system on the backend in order to
 * guide the user from a "site A" to a "site B" in the university
 */
class HighPrecisionRouteActivity : LightSensitiveThemeActivity() {

    private lateinit var highPrecisionRouteViewModel: HighPrecisionRouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // creates model
        val highPrecisionRouteModel = HighPrecisionRouteModel(application)

        // creates view model
        highPrecisionRouteViewModel = ViewModelProvider(
            this,
            HighPrecisionRouteViewModelFactory(highPrecisionRouteModel)
        )[HighPrecisionRouteViewModel::class.java]


        // TODO: Init viewmodel

        setContent {
            ThemeScreen(themeViewModel = themeViewModel) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    HighPrecisionRouteScreen(
                        highPrecisionRouteViewModel = highPrecisionRouteViewModel,
                        backToMainMenu = { finish() },
                        context = LocalContext.current
                    )
                }
            }
        }
    }

}