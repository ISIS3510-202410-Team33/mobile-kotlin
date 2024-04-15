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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ventura.R
import com.example.ventura.ui.theme.VenturaTheme


val smallPadding = 8.dp
val mediumPadding = 16.dp
val largePadding = 24.dp
val mediumIcon = 64.dp

@Composable
fun ProfileScreen() {
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
                    itemText = "John Doe"
                )
            }
            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "Mail",
                    itemIcon = Icons.Default.Email,
                    itemText = "john.doe@mail.com"
                )
            }

            item {
                ProfileItem(
                    modifier = Modifier,
                    itemTitle = "University",
                    itemIcon = Icons.Default.Home,
                    itemText = "Monsters University"
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
                Icon(
                    modifier = modifier
                        .padding(smallPadding),
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to main menu"
                )
                Text(
                    modifier = modifier
                        .padding(smallPadding),
                    text = "Profile",
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
) {
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
            contentDescription = null
        )
        Column {
            Text(
                modifier = modifier
                    .padding(start = smallPadding),
                text = itemTitle,
                onTextLayout = { }
            )
            Text(
                modifier = modifier.padding(smallPadding),
                text = itemText,
                onTextLayout = { }
            )
        }
        Spacer(
            modifier = modifier
                .weight(1f)
                .padding(smallPadding)
        )
        IconButton(
            modifier = modifier
                .padding(smallPadding)
                .size(mediumIcon),
            onClick = { }
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $itemText"
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    VenturaTheme {
        ProfileScreen()
    }
}