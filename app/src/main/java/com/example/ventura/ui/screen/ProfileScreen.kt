@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ventura.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ventura.R
import com.example.ventura.data.models.StepCount
import com.example.ventura.ui.theme.Shapes
import com.example.ventura.ui.viewmodel.ProfileViewModel
import com.example.ventura.ui.viewmodel.ThemeViewModel
import kotlin.math.max
import kotlin.math.min


val smallPadding = 8.dp
val mediumPadding = 16.dp
val largePadding = 24.dp
val extraLargePadding = 36.dp
val smallIcon = 56.dp
val mediumIcon = 64.dp
val maxTextBoxWidth = 200.dp

private val TAG = "PROFILE_SCREEN"


@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel(),
    stepCount: StepCount?,
    dailyStepsObjective: Int,
    dailyCaloriesObjective: Int,
    backToMainMenu: () -> Unit = { },
    context: Context
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val themeUiState by themeViewModel.themeSetting.observeAsState()

    Scaffold (
        topBar = {
            ProfileTopAppBar(
                modifier = Modifier,
                backToMainMenu = backToMainMenu
            )
        }
    ) { it ->
        LazyColumn(
            modifier = Modifier,
            contentPadding = it
        ) {
            item {
                ProfileImage(
                    modifier = Modifier
                )
            }
            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "Name",
                    itemIcon = Icons.Filled.Person,
                    itemText = profileUiState.profile.name,
                    canEdit = true,
                    onBoxExit = { profileViewModel.updateProfileCache() },
                    onNewText = { profileViewModel.changeProfileName(it) }
                )
            }
            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "Email",
                    itemIcon = Icons.Default.Email,
                    itemText = profileUiState.profile.email,
                    canEdit = false,
                    onBoxExit = { profileViewModel.updateProfileCache() },
                    onNewText = { profileViewModel.changeProfileEmail(it) }
                )
            }

            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "University",
                    itemIcon = Icons.Default.Home,
                    itemText = profileUiState.profile.universityName,
                    canEdit = false,
                    onBoxExit = { profileViewModel.updateProfileCache() },
                    onNewText = { profileViewModel.changeProfileUniversity(it) }
                )
            }

            item {
                ThemeSettingSelection(
                    modifier = Modifier,
                    currentTheme = themeUiState!!.setting,
                    onThemeChange = { themeViewModel.updateThemeSetting(it) }
                )
            }

            // Step counting
            item {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    WalkObjectiveBar(
                        title = "We don't have your permission for this functionality.",
                        leftBound = 0,
                        rightBound = 100,
                        currentValue = 0
                    )
                } else if (stepCount == null) {
                    WalkObjectiveBar(
                        title = "We are fetching your daily steps...",
                        leftBound = 0,
                        rightBound = dailyStepsObjective,
                        currentValue = 0
                    )
                } else {
                    Log.d(TAG, "Day, Now, Obj : ${stepCount.stepsAtDayStart}, ${stepCount.stepsAtNow}, ${stepCount.stepsAtDayStart + dailyStepsObjective}")
                    WalkObjectiveBar(
                        title = "Steps taken today: ${
                            stepCount.stepsAtNow - 
                                    stepCount.stepsAtDayStart}",
                        leftBound = stepCount.stepsAtDayStart,
                        rightBound = stepCount.stepsAtDayStart + dailyStepsObjective,
                        currentValue = stepCount.stepsAtNow
                    )
                }
            }

            // Calorie counting
            item {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION) !=
                    PackageManager.PERMISSION_GRANTED) {
                    WalkObjectiveBar(
                        title = "We don't have your permission for this functionality.",
                        leftBound = 0,
                        rightBound = 100,
                        currentValue = 0
                    )
                } else if (stepCount == null) {
                    WalkObjectiveBar(
                        title = "We are fetching your burned calories ...",
                        leftBound = 0,
                        rightBound = dailyCaloriesObjective,
                        currentValue = 0
                    )
                } else {

                    val burnedCalories = ((stepCount.stepsAtNow - stepCount.stepsAtDayStart)*0.045f).toInt()

                    WalkObjectiveBar(
                        title = "Calories burned today: $burnedCalories",
                        leftBound = 0,
                        rightBound = dailyCaloriesObjective,
                        currentValue = burnedCalories
                    )
                }
            }
        }
    }
}


@Composable
private fun ProfileTopAppBar(
    modifier: Modifier = Modifier,
    backToMainMenu: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(mediumPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right

            ) {
                IconButton(
                    modifier = modifier
                        .padding(smallPadding),
                    onClick = backToMainMenu
                ) {
                    Icon(
                        modifier = modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(smallPadding)
                            .size(mediumIcon)
                        ,
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to main menu",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
//                Text(
//                    modifier = modifier
//                        .padding(smallPadding),
//                    text = "Profile",
//                    style = MaterialTheme.typography.displayLarge,
//                    color = MaterialTheme.colorScheme.secondary
//                )
            }
        },
        modifier = modifier.background(Color.Transparent)
    )
}


@Composable
private fun ProfileImage(
    modifier: Modifier = Modifier
) {
    Row (
        modifier = modifier
            .padding(
                start = smallPadding,
                top = largePadding,
                end = smallPadding,
                bottom = mediumPadding
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    )
    {
        Image(
            modifier = modifier
                .clip(CircleShape)
                .size(200.dp)
            ,
            painter = painterResource(R.drawable.john_doe_profile_picture),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}


@Preview
@Composable
private fun TestRow() {
    Row(
        modifier = Modifier
            .padding(
                start = mediumPadding,
                end = mediumPadding,
                top = smallPadding,
                bottom = smallPadding
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .height(30.dp),

    ) {
        Icon(
            modifier = Modifier
                .size(mediumIcon),
            imageVector = Icons.Default.Email,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            contentDescription = null
        )
    }
}


@Composable
private fun ProfileItem(
    modifier: Modifier = Modifier,
    itemTitle: String,
    itemIcon: ImageVector,
    itemText: String,
    canEdit: Boolean,
    onNewText: (String) -> Unit,
    onBoxExit: () -> Unit
) {
    val isEditing = remember { mutableStateOf(false) }

    Row (
        modifier = modifier
            .padding(
                start = largePadding,
                end = largePadding,
                top = smallPadding,
                bottom = smallPadding
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.primaryContainer)

            .zIndex(1f)
        ,
        verticalAlignment = Alignment.CenterVertically,


    ) {
        Icon(
            modifier = modifier
                .size(mediumIcon)
                .padding(start = mediumPadding),
            imageVector = itemIcon,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            contentDescription = null
        )
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = modifier
                    .padding(start = mediumPadding),
                text = itemTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                onTextLayout = { }
            )

            if (isEditing.value) {
                OutlinedTextField(
                    value = itemText,
                    singleLine = true,
                    shape = Shapes.large,
                    modifier = modifier
                        .padding(smallPadding)
                        .width(maxTextBoxWidth),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onValueChange = onNewText,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onBoxExit()
                            isEditing.value = false
                        }
                    )
                )
            }
            else {
                Text(
                    modifier = modifier
                        .padding(start = mediumPadding)
                        .width(maxTextBoxWidth),
                    text = itemText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    onTextLayout = { }
                )
            }
        }
        Spacer(
            modifier = modifier
                .weight(2f)
                .padding(
                    start = smallPadding,
                    top = smallPadding,
                    bottom = smallPadding,
                    end = largePadding
                )
        )
        IconButton(
            modifier = modifier
                .padding(smallPadding),
            onClick = {
                isEditing.value = true
            },
            enabled = canEdit
        ) {
            Icon(
                modifier = modifier
                    .size(mediumIcon),
                imageVector = Icons.Default.Edit,
                tint = if (canEdit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error,
                contentDescription = "Edit $itemText"
            )
        }
    }
}


@Composable
private fun ThemeSettingButton(
    modifier: Modifier = Modifier,
    themeKey: String,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    content: @Composable () -> Unit
) {
    // system theme
    Button(
        modifier = modifier
            .padding(smallPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (currentTheme == themeKey) {
                // Highlight color for selected state
                MaterialTheme.colorScheme.primaryContainer
            } else {
                // Default color for unselected state
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (currentTheme == themeKey) {
                // Adjust content color for contrast
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        ),

        onClick = { onThemeChange(themeKey) }
    ) {
        content()
    }
}


@Composable
private fun ThemeSettingSelection(
    modifier: Modifier = Modifier,
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Column (
        modifier = modifier
            .padding(smallPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeSettingButton(
                themeKey = "system",
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            ) {
                Text("System")
            }

            ThemeSettingButton(
                themeKey = "light_sensitive",
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            ) {
                Text("Light sensitive")
            }
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeSettingButton(
                themeKey = "light",
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            ) {
                Text("Light")
            }

            ThemeSettingButton(
                themeKey = "dark",
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            ) {
                Text("Dark")
            }
        }
    }
}


@Composable
private fun WalkObjectiveBar(
    modifier: Modifier = Modifier,
    title: String,
    leftBound: Int,
    rightBound: Int,
    currentValue: Int
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            modifier = Modifier,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelSmall
        )

        Row (
            modifier = Modifier
                .padding(
                    start = largePadding,
                    end = largePadding,
                    top = smallPadding,
                    bottom = smallPadding
                )
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxWidth()
                .height(30.dp),
        ) {

            val percentFilled = min(
                0.9999f,
                (currentValue.toFloat() - leftBound.toFloat())
                        /(rightBound.toFloat()-leftBound.toFloat())
            )

            Row (
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    .weight(max(0.0001f, percentFilled))
                    .height(30.dp),
            ) {}
            Spacer(
                modifier = Modifier
                    .weight(max(0.0001f, 1f - percentFilled))
            )
        }
    }
}



@Preview
@Composable
fun WalkObjectiveBarPreview() {
    WalkObjectiveBar(title="Pasos caminados", leftBound = 0, rightBound = 1000, currentValue = 8000)
}



//@Preview
//@Composable
//fun ThemeSettingSelectionPreview() {
//    ThemeSettingSelection(
//        currentTheme = "dark",
//        onThemeChange = { }
//    )
//}


//@Preview
//@Composable
//fun ProfileItemPreview() {
//    ProfileItem(
//        itemTitle = "Name",
//        itemIcon = Icons.Default.Email,
//        itemText = "john.doe@university.com",
//        canEdit = false,
//        onNewText = {}
//    ) {
//
//    }
//}


//@Preview
//@Composable
//fun ProfileScreenPreviewLight() {
//    ThemeScreen(darkTheme = false) {
//        ProfileScreen() { }
//    }
//}
//
//
//@Preview
//@Composable
//fun ProfileScreenPreviewDark() {
//    ThemeScreen(darkTheme = true) {
//        ProfileScreen() { }
//    }
//}
