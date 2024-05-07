package com.example.ventura.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.PermanentSensorsApplication
import com.example.ventura.ui.viewmodel.ThemeViewModel
import com.example.ventura.ui.viewmodel.ThemeViewModelFactory


private val TAG = "SENSITIVE_THEME_ACTIVITY"


open class LightSensitiveThemeActivity : ComponentActivity() {

    protected lateinit var themeViewModel: ThemeViewModel


    /*
     this constructor initializes the theme view model by using the already initialized repositories
     in the application scope. When a class inherits this activity, it will inherit its
     themeViewModel construction, which will allow it to access the theme view model's attributes
     in order to correctly choose and modify the theme
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeViewModel = ViewModelProvider(
            this,
            ThemeViewModelFactory(
                (application as PermanentSensorsApplication).lightRepository,
                (application as PermanentSensorsApplication).themeRepository
            )
        )[ThemeViewModel::class.java]
    }
}