@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ventura.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ventura.R
import com.example.ventura.ui.theme.Shapes
import com.example.ventura.ui.theme.VenturaTheme
import com.example.ventura.viewmodel.ProfileViewModel


val smallPadding = 8.dp
val mediumPadding = 16.dp
val largePadding = 24.dp
val mediumIcon = 64.dp

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel = viewModel()) {
    val profileUiState by profileViewModel.uiState.collectAsState()

    Scaffold (
        topBar = {
            ProfileTopAppBar()
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
                    onBoxExit = { profileViewModel.updateProfileData() },
                    onNewText = { profileViewModel.changeProfileName(it) }
                )
            }
            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "Mail",
                    itemIcon = Icons.Default.Email,
                    itemText = profileUiState.profile.email,
                    onBoxExit = { profileViewModel.updateProfileData() },
                    onNewText = { profileViewModel.changeProfileEmail(it) }
                )
            }

            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "University",
                    itemIcon = Icons.Default.Home,
                    itemText = profileUiState.profile.universityName,
                    onBoxExit = { profileViewModel.updateProfileData() },
                    onNewText = { profileViewModel.changeProfileUniversity(it) }
                )
            }
        }
    }
}


@Composable
private fun ProfileTopAppBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = modifier
                        .padding(smallPadding),
                    onClick = { }
                ) {
                    Icon(
                        modifier = modifier
                            .size(mediumIcon),
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to main menu",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    modifier = modifier
                        .padding(smallPadding),
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        modifier = modifier
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


@Composable
private fun ProfileItem(
    modifier: Modifier = Modifier,
    itemTitle: String,
    itemIcon: ImageVector,
    itemText: String,
    onNewText: (String) -> Unit,
    onBoxExit: () -> Unit
) {
    val isEditing = remember { mutableStateOf(false) }

    Row (
        modifier = modifier
            .padding(smallPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = modifier
                .padding(smallPadding)
                .size(mediumIcon),
            imageVector = itemIcon,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = null
        )
        Column {
            Text(
                modifier = modifier
                    .padding(start = smallPadding),
                text = itemTitle,
                style = MaterialTheme.typography.labelLarge,
                onTextLayout = { }
            )

            if (isEditing.value) {
                OutlinedTextField(
                    value = itemText,
                    singleLine = true,
                    shape = Shapes.large,
                    modifier = modifier
                        .padding(smallPadding)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface
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
                    modifier = modifier.padding(smallPadding),
                    text = itemText,
                    style = MaterialTheme.typography.bodyLarge,
                    onTextLayout = { }
                )
            }
        }
        Spacer(
            modifier = modifier
                .weight(1f)
                .padding(smallPadding)
        )
        IconButton(
            modifier = modifier
                .padding(smallPadding),
            onClick = {
                isEditing.value = true
            }
        ) {
            Icon(
                modifier = modifier
                    .size(mediumIcon),
                imageVector = Icons.Default.Edit,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = "Edit $itemText"
            )
        }
    }
}


@Preview
@Composable
fun ProfileScreenPreviewLight() {
    VenturaTheme(darkTheme = false) {
        ProfileScreen()
    }
}


@Preview
@Composable
fun ProfileScreenPreviewDark() {
    VenturaTheme(darkTheme = true) {
        ProfileScreen()
    }
}