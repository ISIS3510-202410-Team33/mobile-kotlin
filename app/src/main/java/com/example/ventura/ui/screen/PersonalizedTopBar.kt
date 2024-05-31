package com.example.ventura.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizedTopBar(
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
            }
        },
    modifier = modifier.background(Color.Transparent)
    )
}