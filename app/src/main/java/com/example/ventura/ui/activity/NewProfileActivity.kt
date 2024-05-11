package com.example.ventura.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.PermanentSensorsApplication
import com.example.ventura.ui.screen.ProfileScreen
import com.example.ventura.ui.theme.ThemeScreen
import com.example.ventura.ui.viewmodel.ProfileViewModel
import com.example.ventura.ui.viewmodel.ProfileViewModelFactory
import com.example.ventura.ui.viewmodel.StepCounterViewModel
import com.example.ventura.ui.viewmodel.StepCounterViewModelFactory


private val TAG = "NewProfileActivity"


class NewProfileActivity : LightSensitiveThemeActivity() {

    private lateinit var stepCounterViewModel: StepCounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val profileViewModel: ProfileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(application)
        )[ProfileViewModel::class.java]

        stepCounterViewModel = ViewModelProvider(
            this,
            StepCounterViewModelFactory((application as PermanentSensorsApplication).stepCounterRepository)
        )[StepCounterViewModel::class.java]

        stepCounterViewModel.stepCount.observe(this) { stepCount ->
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
                            stepCount = stepCount,
                            dailyStepsObjective = stepCounterViewModel.getDailyStepsObjective(),
                            dailyCaloriesObjective = stepCounterViewModel.getDailyCaloriesObjective(),
                            backToMainMenu = { finish() },
                            context = this
                        )
                    }
                }
            }
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreviewLight() {
//    ThemeScreen(darkTheme=false) {
//        ProfileScreen { }
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreviewDark() {
//    ThemeScreen(darkTheme = true) {
//        ProfileScreen { }
//    }
//}