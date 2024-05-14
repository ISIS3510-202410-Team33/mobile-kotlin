package com.example.ventura.ui.screen

//import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ventura.data.models.Building
import com.example.ventura.data.models.Site
import com.example.ventura.data.remote.SiteResponse
import com.example.ventura.ui.viewmodel.HighPrecisionRouteViewModel

private val TAG = "HighPrecisionRouteScreen"


@Composable
fun HighPrecisionRouteScreen(
    backToMainMenu: () -> Unit = { },
    highPrecisionRouteViewModel: HighPrecisionRouteViewModel,
    context: Context
) {
    val highPrecisionRouteUiState by highPrecisionRouteViewModel.uiState.collectAsState()

    val lazyListState = rememberLazyListState()

    Scaffold (
        topBar = {
            PersonalizedTopBar(
                modifier = Modifier,
                backToMainMenu = backToMainMenu
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = mediumPadding))
        }

    ) {
        LazyColumn(
            modifier = Modifier.padding(top= smallPadding),
            contentPadding = it,
            horizontalAlignment = Alignment.CenterHorizontally,
            state = lazyListState
        ) {
            // Building selection
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExposedDropdownMenuBuildingSelection(
                        guideText = "Starting building",
                        elements = highPrecisionRouteUiState.buildings,
                        onSelectedChoice = { highPrecisionRouteViewModel.getFromSitesByBuilding(it) }
                    )
                    ExposedDropdownMenuBuildingSelection(
                        guideText = "Destination building",
                        elements = highPrecisionRouteUiState.buildings,
                        onSelectedChoice = { highPrecisionRouteViewModel.getToSitesByBuilding(it) }
                    )
                }
            }

            // Site selection
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExposedDropdownMenuSiteSelection(
                        guideText = "Starting site",
                        elements = highPrecisionRouteUiState.sitesFrom,
                        onSelectedChoice = { highPrecisionRouteViewModel.setFromSite(it) }
                    )
                    ExposedDropdownMenuSiteSelection(
                        guideText = "Destination site",
                        elements = highPrecisionRouteUiState.sitesTo,
                        onSelectedChoice = { highPrecisionRouteViewModel.setToSite(it) }
                    )
                }
            }

            // Take me there button
            item {
                TakeMeThereButton(
                    selected = (
                            highPrecisionRouteUiState.selectedFromSite != null
                                    && highPrecisionRouteUiState.selectedToSite != null
                            ),
                    onSitesSelected = { highPrecisionRouteViewModel.getRouteSites() },
                    context = context,
                    modifier = Modifier
                        .padding(
                            start = largePadding,
                            top = mediumPadding,
                            end = largePadding,
                            bottom = smallPadding
                        )
                )
            }

            // Route information
            item {
                if (highPrecisionRouteUiState.currentNodePath == null) {
                    // unavailable
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                    }
                    Text(
                        text = "Follow the guiding images to your destination here!",
                        modifier = Modifier.padding(largePadding),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GuidingInstruction(
                            currentSite = highPrecisionRouteUiState.route!!.sites[highPrecisionRouteUiState.currentNodeIndex!!],
                            totalDistance = highPrecisionRouteUiState.route!!.distance,
                            modifier = Modifier
                                .padding(
                                    start = largePadding,
                                    top = smallPadding,
                                    end = largePadding,
                                    bottom = smallPadding
                                )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceAround
                    ) {

                        MoveSiteImageButton(
                            onClick = { highPrecisionRouteViewModel.setPreviousNode() },
                            disableCondition = highPrecisionRouteUiState.currentNodeIndex == 0,
                            imageVector = Icons.Default.KeyboardArrowLeft
                        )
                        SiteImageNetwork(url = highPrecisionRouteUiState.currentNodePath!!, context = context)
                        MoveSiteImageButton(
                            onClick = { highPrecisionRouteViewModel.setNextNode() },
                            disableCondition = highPrecisionRouteUiState.currentNodeIndex == highPrecisionRouteUiState.route!!.sites.size - 1,
                            imageVector = Icons.Default.KeyboardArrowRight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuidingInstruction(
    modifier: Modifier = Modifier,
    currentSite: SiteResponse,
    totalDistance: Int
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { },
            enabled = true,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Total distance: $totalDistance meters",
                modifier = modifier,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = { },
            enabled = true,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Head to ${currentSite.name}",
                modifier = modifier,
                textAlign = TextAlign.Center,
            )
        }
    }

}


// TODO: Check correct functionality
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBuildingSelection(
    guideText: String = "",
    elements: List <Building>,
    onSelectedChoice: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .padding(16.dp)
            .width(150.dp)

    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.background(Color.Transparent)
        ) {
            // todavía no ha cargado los datos
            TextField(
                value = selectedText,
                onValueChange = { selectedText = it },
                label = { Text(text = guideText) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .background(Color.Transparent),
                maxLines = 3
            )

            val filteredOptions =
                elements.filter { it.name.contains(selectedText, ignoreCase = true) }

            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { }
                ) {
                    filteredOptions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item.name) },
                            onClick = {
                                onSelectedChoice(item.id)
                                selectedText = item.name
                                expanded = !expanded
                            },
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuSiteSelection(
    guideText: String = "",
    elements: List <Site>,
    onSelectedChoice: (Site) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .padding(16.dp)
            .width(150.dp)

    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.background(Color.Transparent)
        ) {
            // todavía no ha cargado los datos
            TextField(
                value = selectedText,
                onValueChange = { selectedText = it },
                label = { Text(text = guideText) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .background(Color.Transparent),
                maxLines = 3
            )

            val filteredOptions =
                elements.filter { it.name.contains(selectedText, ignoreCase = true) }

            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { }
                ) {
                    filteredOptions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item.name) },
                            onClick = {
                                onSelectedChoice(item)
                                selectedText = item.name
                                expanded = !expanded
                            },
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TakeMeThereButton(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSitesSelected: () -> Unit,
    context: Context
) {
    Button(
        onClick = {
            if (!selected) {
                Toast.makeText(
                    context,
                    "You have not selected your sites yet!",
                    Toast.LENGTH_SHORT
                ).show()
            } else onSitesSelected()
        },
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text("Take me there!")
    }
}


@Composable
fun MoveSiteImageButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    disableCondition: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        // Disable the button if isFirst is true
        enabled = !disableCondition,
        onClick = {
            if (!disableCondition) {
                onClick()
            }
        }
    ) {
        Icon(
            modifier = modifier
                .size(mediumIcon),
            imageVector = imageVector,
            contentDescription = "Previous Site"
        )
    }
}


@Composable
fun SiteImageNetwork(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    context: Context
) {
    val newUrl = "http://192.168.1.187:42069$url"
//    Log.d(TAG, "Trying GlideImage with url = $newUrl")
//
//    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
//
//    LaunchedEffect(url) {
//        with(context) {
//            Glide.with(this)
//                .load(newUrl)
//                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)) // Cache la imagen para usos futuros
//                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable?> {
//                    override fun onLoadFailed(
//                        e: com.bumptech.glide.load.engine.GlideException?,
//                        model: Any?,
//                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable?>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        // Si falla la carga, mostrar el texto de conexión
//
//                        return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: android.graphics.drawable.Drawable?,
//                        model: Any?,
//                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable?>?,
//                        dataSource: com.bumptech.glide.load.DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        // Si la imagen se carga correctamente, mostrarla y ocultar el texto de conexión
//
//                        return false
//                    }
//                })
//                .into()
//        }
//    }
//
//    if (imageBitmap != null) {
//        Image(
//            modifier = modifier,
//            contentDescription = contentDescription,
//            bitmap = imageBitmap!!
//        )
//    } else {
//        // Show placeholder while image is loading (optional)
//        Box(modifier) {
//            // Your placeholder UI
//            Text("We're loading your route...")
//            Modifier.padding(largePadding)
//        }
//    }

    AsyncImage (
        model = newUrl,
        contentDescription = contentDescription
    )
}

//@OptIn(ExperimentalGlideComposeApi::class)
//@Composable
//fun SiteImageNetwork(
//    url: String,
//    modifier: Modifier = Modifier,
//    contentDescription: String? = null,
//) {
//    val newUrl = "http://192.168.1.187:42069" + url
//    Log.d(TAG, "Trying GlideImage with url = $newUrl")
//    GlideImage(
//        model = newUrl,
//        contentDescription = contentDescription,
//        modifier = modifier
//    )
//}