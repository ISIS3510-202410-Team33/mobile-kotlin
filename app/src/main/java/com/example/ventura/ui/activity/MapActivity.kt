package com.example.ventura.ui.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ventura.R
import com.example.ventura.ui.viewmodel.CampusLocationsViewModelFactory
import com.example.ventura.ui.viewmodel.CampusLocationsViewModel
import com.example.ventura.ui.viewmodel.RatingViewModel
import com.example.ventura.ui.viewmodel.UserPreferencesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.app.AlertDialog
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.ventura.ui.viewmodel.RatingViewModelFactory
import com.example.ventura.ui.viewmodel.UserPreferencesViewModelFactory

class MapsActivity : AppCompatActivity() {

    // Variables to storage the components of the location provider
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var jsonOb: JSONObject? = null
    private var lastKnownLocation: Location? = null
    private var presentedFetchDate: Boolean = true

    // Variable to support swipe down
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // List of strings of added locations
    private val addedLocations = mutableListOf<String>()
    

    // Variables to storage coordinates and distances
    private var userCoordinates: Pair<Double, Double>? = null
    private var buildingCoordinates: Pair<Double, Double>? = null
    private var distanceToBuilding: Float? = null

    // Average delay in km/h on city
    private var averageWalkingDelay: Float = 5F
    private var averageBikeDelay: Float = 3F
    private var averageCarDelay: Float = 2F

    // Lateinit vars to use the corresponding ViewModels
    private lateinit var jsonViewModel: CampusLocationsViewModel
    private lateinit var userPrefViewModel: UserPreferencesViewModel
    private lateinit var ratingViewModel: RatingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Retrieve the email from the extras of the intent
        val userEmail = intent.getStringExtra("user_email")

        jsonViewModel = ViewModelProvider(this, CampusLocationsViewModelFactory(this)).get(
            CampusLocationsViewModel::class.java)
        userPrefViewModel = ViewModelProvider(this, UserPreferencesViewModelFactory(this)).get(
            UserPreferencesViewModel::class.java)
        ratingViewModel = ViewModelProvider(this, RatingViewModelFactory(this)).get(RatingViewModel::class.java)

        // allows the user to swipe down to update locations
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            AlertDialog.Builder(this)
                .setTitle("Update Maps")
                .setMessage("Do you want to update the campus location list?")
                .setPositiveButton("Yes") { _, _ ->
                    if (!isNetworkAvailable()) {
                        AlertDialog.Builder(this)
                            .setTitle("No Internet Connection")
                            .setMessage("Cannot update campus locations without an internet connection.")
                            .setPositiveButton("OK") { _, _ ->
                                swipeRefreshLayout.isRefreshing = false
                            }
                            .show()
                    } else {
                        lifecycleScope.launch {
                            try {
                                jsonViewModel.updateJsonData()
                                swipeRefreshLayout.isRefreshing = false

                                // Display a Toast message
                                Toast.makeText(this@MapsActivity, "Campus locations updated successfully", Toast.LENGTH_LONG).show()
                                
                                // refresh the activity
                                removeAllLocations()

                                // discard the previous data
                                jsonOb = null
                                

                                showLocations(userEmail!!)


                            } catch (e: Exception) {
                                swipeRefreshLayout.isRefreshing = false
                                // Display a Toast message
                                Toast.makeText(this@MapsActivity, "Couldn't update the maps: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNegativeButton("No") { _, _ ->
                    swipeRefreshLayout.isRefreshing = false
                }
                .show()
        }

        // Using lifecycleScope to fetch JSON data
        lifecycleScope.launch {
            showLocations(userEmail!!)
        }

    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun generateRecommendations(mostVisited: String, spaces: JSONObject): List<String> {

        if (mostVisited.isEmpty() || mostVisited.isBlank()) {
            Toast.makeText(this, "Go explore the campus to get recommendations!", Toast.LENGTH_SHORT).show()
            return emptyList()
        }

        val recommendations = mutableListOf<String>()
        for (spaceKey in spaces.keys()) {
            if (spaceKey.contains(mostVisited)) {
                recommendations.add(spaceKey)
                Log.d("Recommendation", spaceKey)
            }
        }
        return recommendations
    }

    private fun isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
    }

    private fun abrirGoogleMaps(latitud: Double, longitud: Double) {
        val uri = "geo:$latitud,$longitud?q=$latitud,$longitud"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Manejar el caso en el que Google Maps no esté instalado en el dispositivo
            // o no haya una actividad que maneje el intent
            val mapUrl = "https://www.google.com/maps/search/?api=1&query=$latitud,$longitud"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            startActivity(mapIntent)
        }
    }

    fun Context.dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun obtenerInformacionAdicional(spaceObject: JSONObject, distanceInMeters: Float?, distanceInKilometers: Float?, timeWak: Float?, timeCar: Float?, timeBike: Float?): String {
        val cantidadPisos = spaceObject.getInt("cantidad_pisos")
        val obstrucciones = spaceObject.getBoolean("obstrucciones")
        val cantidadRestaurantes = spaceObject.getInt("cantidad_restaurantes")
        val cantidadZonasVerdes = spaceObject.getInt("cantidad_zonas_verdes")

        val distanceString = if (distanceInMeters != null && distanceInKilometers != null) {
            "Distance to this location: $distanceInMeters m (${String.format("%.2f", distanceInKilometers)} km)\n"
        } else {
            "Distance to this location: Desconocida\n"
        }

        val timeWakMin = if (distanceInMeters != null && distanceInKilometers != null) {
            "Travel time walking: ${String.format("%.2f", timeWak)} min\n"
        } else {
            "Travel time walking: Desconocido\n"
        }

        val timeCarMin = if (distanceInMeters != null && distanceInKilometers != null) {
            "Travel time by car: ${String.format("%.2f", timeCar)} min\n"
        } else {
            "Travel time by car: Desconocido\n"
        }

        val timeBikeMin = if (distanceInMeters != null && distanceInKilometers != null) {
            "Travel time by bike: ${String.format("%.2f", timeBike)} min\n"
        } else {
            "Travel time by bike: Desconocido\n"
        }
        var obstrucciones2 = if (obstrucciones) "Yes" else "None"
        return "Floors: $cantidadPisos \nRestaurants: $cantidadRestaurantes \n" +
                "Obstructions: $obstrucciones2 \nGreen areas: $cantidadZonasVerdes\n$distanceString" +
                timeWakMin + timeCarMin + timeBikeMin
    }

    suspend fun showLocations(userEmail: String) {
        try {


            val (jsonObject, message) = jsonViewModel.fetchJsonData()
            // Handle the obtained JSONObject here, update your UI accordingly
            // Displaying a Toast message

            if (presentedFetchDate){
                Toast.makeText(this@MapsActivity, message, Toast.LENGTH_LONG).show()
                presentedFetchDate = false
            }

            // If jsonObject is not null, update UI or do further processing
            jsonObject?.let {
                // Update UI or process data

                //jsonOb = jsonObject

                val buttonBackToMenu = findViewById<ImageView>(R.id.buttonBackToMenu)
                buttonBackToMenu.setOnClickListener {

                    finish() // Optional: finishes current activity to free resources
                }

                locationRequest = LocationRequest.create().apply {
                    interval =
                        10000 // Set the desired interval for active location updates, in milliseconds.
                    fastestInterval =
                        5000 // Set the fastest rate for active location updates, in milliseconds.
                    priority =
                        LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority of the request.
                }

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        for (location in locationResult.locations) {
                            // Update UI with location data
                            // Call a method to handle the current location.

                            lastKnownLocation = location // Guardar la última ubicación conocida
                            userCoordinates = Pair(location.latitude, location.longitude)
                            Log.d("Location", "$lastKnownLocation")

                        }
                    }
                }

                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this@MapsActivity)

                if (ActivityCompat.checkSelfPermission(
                        this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request location permissions
                    ActivityCompat.requestPermissions(
                        this@MapsActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                } else {
                    // Permissions are already granted, start location updates
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }

                val linearLayout = findViewById<LinearLayout>(R.id.linear_layout_locations)




                val spaces = jsonObject.getJSONObject("spaces")

                if (spaces != null) {


                    /*
                     * <-- BQ 1 -->
                     *
                     * Here we use the ViewModel in order to observe if the user recommendations json
                     * has been updated in which case we update the recommended buildings to be shown
                     * in the activity to the user. So, we are using the live data component to keep
                     * data updated and allow reactive communication between different
                     * parts of the application
                     *
                     */

                    val obtainedRecommendationsLiveData = MutableLiveData<List<String>>()

                    // create recomendations
                    userPrefViewModel.getData(this@MapsActivity, userEmail)
                    userPrefViewModel.data.observe(this@MapsActivity, Observer { jsonObject: JSONObject ->

                            Log.d("recomendation", "started")
                            Log.d("Recomendation visited", jsonObject.toString())

                            var maxEntry = Pair("", 0)
                            for (key in jsonObject.keys()) {
                                val value = jsonObject.getInt(key)
                                if (value > maxEntry.second) {
                                    maxEntry = Pair(key, value)
                                }
                            }
                            Log.d("Recomendation most visited", maxEntry.first.toString())
                            val obtainedRecommendations =
                                generateRecommendations(maxEntry.first, spaces)
                            Log.d(
                                "FORMAT RECOMENDATION - ALL",
                                obtainedRecommendations.toString()
                            )

                            // Update the LiveData variable
                            obtainedRecommendationsLiveData.postValue(obtainedRecommendations)
                        })

                    /*
                    * <-- BQ 4 -->
                    *
                    * Here we use the rates ViewModel in order to observe if there are any updates
                    * in the best rated building so it can be shown by the user.
                    *
                    */

                    ratingViewModel.obtenerEdificioConMejorPuntaje()
                        .observe(this@MapsActivity, Observer { mejorEdificio ->
                            // Observe the Live Data to get updates
                            obtainedRecommendationsLiveData.observe(
                                this@MapsActivity,
                                Observer { recommendations ->

                                    for (spaceKey in spaces.keys()) {

                                        // if the name of the location is already in the list, we skip it
                                        if (addedLocations.contains(spaceKey)) {
                                            continue
                                        }

                                        // add the location to the list of added locations
                                        addedLocations.add(spaceKey)


                                        

                                            val textLayout = LinearLayout(this@MapsActivity)
                                        textLayout.orientation = LinearLayout.VERTICAL
                                        // Change orientation to
                                        // Establece los parámetros de diseño del LinearLayout
                                        val layoutParams1 = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )

                                        // Establece los márgenes del LinearLayout (izquierda, arriba, derecha, abajo)
                                        layoutParams1.setMargins(100, 32, 100, 32)

                                        // Aplica los parámetros de diseño al LinearLayout
                                        textLayout.layoutParams = layoutParams1

                                        // Establece la elevación del LinearLayout
                                        textLayout.elevation =
                                            resources.getDimension(R.dimen.elevation_3dp)

                                        // Establece el fondo del LinearLayout

                                        textLayout.setBackgroundResource(R.drawable.building_container)

                                        // Establece el relleno del LinearLayout
                                        textLayout.setPadding(16, 16, 16, 16)


                                        val layoutParams2 = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, // Adjust width to match parent
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams2.gravity = Gravity.CENTER_VERTICAL

                                        val textView = TextView(this@MapsActivity)
                                        textView.text = spaceKey
                                        textView.setTextColor(
                                            ContextCompat.getColor(
                                                this@MapsActivity,
                                                android.R.color.black
                                            )
                                        )
                                        textView.setTypeface(
                                            Typeface.create(
                                                "Lato-Light",
                                                Typeface.NORMAL
                                            )
                                        )
                                        textView.setTextSize(
                                            TypedValue.COMPLEX_UNIT_SP,
                                            20F
                                        ) // Set text size to 20sp
                                        textView.layoutParams = layoutParams2

                                        val verMas = Button(this@MapsActivity)
                                        verMas.text = "View more information"
                                        verMas.backgroundTintList =
                                            ColorStateList.valueOf(Color.parseColor("#363c45"))

                                        val button = Button(this@MapsActivity)
                                        button.text = "Locate in map"
                                        button.backgroundTintList =
                                            ColorStateList.valueOf(Color.parseColor("#363c45")) // Set button background color to red

                                        val buttonCalificar = Button(this@MapsActivity)
                                        buttonCalificar.text = "Rate this location!"
                                        button.backgroundTintList =
                                            ColorStateList.valueOf(Color.parseColor("#363c45"))

                                        val infoView = TextView(this@MapsActivity)
                                        infoView.visibility = View.GONE
                                        infoView.setTextColor(
                                            ContextCompat.getColor(
                                                this@MapsActivity,
                                                android.R.color.black
                                            )
                                        )
                                        infoView.setTypeface(
                                            Typeface.create(
                                                "Lato-Light",
                                                Typeface.NORMAL
                                            )
                                        )

                                        val isRecommended = recommendations.contains(spaceKey)
                                        val isBestRated = mejorEdificio.contains(spaceKey)

                                        if (isRecommended) {
                                            val recommendedLayout = LinearLayout(this@MapsActivity)
                                            recommendedLayout.orientation = LinearLayout.HORIZONTAL

                                            val recommendedMessage = ImageView(this@MapsActivity)
                                            recommendedMessage.setBackgroundResource(R.drawable.gps_glass)

                                            // Create LayoutParams for the ImageView with width and height of 16dp
                                            val imageLayoutParams = LinearLayout.LayoutParams(
                                                dpToPx(20), // Convert 16dp to pixels
                                                dpToPx(23)
                                            )

                                            // Apply LayoutParams to the ImageView
                                            recommendedMessage.layoutParams = imageLayoutParams

                                            recommendedMessage.setOnClickListener {
                                                // Here you put the code to display a popup message when the image is clicked
                                                val toast = Toast.makeText(
                                                    this@MapsActivity,
                                                    "¡Recommended location for you!",
                                                    Toast.LENGTH_SHORT
                                                )


                                                toast.setGravity(Gravity.CENTER, 0, 0)
                                                toast.show()
                                            }

                                            val recommendedText = TextView(this@MapsActivity)
                                            recommendedText.text = "  Recommended location!"
                                            recommendedText.setTextColor(Color.BLACK) // Change text color to black
                                            recommendedText.layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )


                                            recommendedLayout.addView(recommendedMessage) // Add recommended message here
                                            recommendedLayout.addView(recommendedText) // Add recommended text here

                                            textLayout.addView(recommendedLayout) // Add the layout to the parent layout
                                        }

                                        /*
                                   * <-- BQ 4 -->
                                   *
                                   * We show to the user which is the best rated location/s or the ones
                                   * with the most positive feedback
                                   *
                                */

                                        if (isBestRated) {
                                            val ratedLayout = LinearLayout(this@MapsActivity)
                                            ratedLayout.orientation = LinearLayout.HORIZONTAL

                                            val ratedMessage = ImageView(this@MapsActivity)
                                            ratedMessage.setBackgroundResource(R.drawable.best_rated)

                                            // Create LayoutParams for the ImageView with width and height of 18dp
                                            val imageLayoutParams = LinearLayout.LayoutParams(
                                                dpToPx(18), // Convert 18dp to pixels
                                                dpToPx(25)
                                            )

                                            // Apply LayoutParams to the ImageView
                                            ratedMessage.layoutParams = imageLayoutParams

                                            ratedMessage.setOnClickListener {
                                                // Here you put the code to display a popup message when the image is clicked
                                                val toast = Toast.makeText(
                                                    this@MapsActivity,
                                                    "¡Best rated place!",
                                                    Toast.LENGTH_SHORT
                                                )
                                                toast.setGravity(Gravity.CENTER, 0, 0)
                                                toast.show()
                                            }

                                            val ratedText = TextView(this@MapsActivity)
                                            ratedText.text = "  Best rated location!"
                                            ratedText.setTextColor(Color.BLACK) // Set text color to black
                                            ratedText.layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            ratedLayout.addView(ratedMessage) // Add rated message here
                                            ratedLayout.addView(ratedText) // Add rated text here
                                            
                                            textLayout.setBackgroundResource(R.drawable.university_button3)
                                            textLayout.addView(ratedLayout) // Add the layout to the parent layout
                                        }

                                        textLayout.addView(textView)
                                        textLayout.addView(button)
                                        textLayout.addView(buttonCalificar)
                                        textLayout.addView(verMas)

                                        linearLayout.addView(textLayout)

                                        verMas.setOnClickListener {
                                            if (infoView.visibility == View.VISIBLE) {
                                                infoView.visibility = View.GONE
                                                verMas.text = "View more information"
                                            } else {
                                                infoView.visibility = View.VISIBLE
                                                verMas.text = "View less"

                                                // Obtener las coordenadas del edificio
                                                val spaceObject = spaces.getJSONObject(spaceKey)
                                                val coordenadasEdificio =
                                                    spaceObject.getJSONArray("coordenadas")
                                                val latitudEdificio =
                                                    coordenadasEdificio.getDouble(0)
                                                val longitudEdificio =
                                                    coordenadasEdificio.getDouble(1)
                                                buildingCoordinates =
                                                    Pair(latitudEdificio, longitudEdificio)

                                                // Calcular la distancia entre las coordenadas del usuario y las del edificio
                                                userCoordinates?.let { userCoords ->
                                                    buildingCoordinates?.let { buildingCoords ->
                                                        val results = FloatArray(1)
                                                        Location.distanceBetween(
                                                            userCoords.first,
                                                            userCoords.second,
                                                            buildingCoords.first,
                                                            buildingCoords.second,
                                                            results
                                                        )
                                                        distanceToBuilding = results[0]

                                                        // Convertir la distancia a metros y kilómetros
                                                        val distanceInMeters =
                                                            distanceToBuilding
                                                        val distanceInKilometers =
                                                            distanceToBuilding?.div(1000)
                                                        val wakExpectedTimeMin =
                                                            distanceInKilometers?.times(
                                                                averageWalkingDelay
                                                            )
                                                        val carExpectedTimeMin =
                                                            distanceInKilometers?.times(
                                                                averageCarDelay
                                                            )
                                                        val bikeExpectedTimeMin =
                                                            distanceInKilometers?.times(
                                                                averageBikeDelay
                                                            )

                                                        // Actualizar la información adicional
                                                        val infoText =
                                                            obtenerInformacionAdicional(
                                                                spaceObject,
                                                                distanceInMeters,
                                                                distanceInKilometers,
                                                                wakExpectedTimeMin,
                                                                carExpectedTimeMin,
                                                                bikeExpectedTimeMin
                                                            )
                                                        infoView.text = infoText

                                                    }
                                                }


                                            }
                                        }

                                        /*
                                 * <-- BQ 1 -->
                                 *
                                 * Here is the "locate in map" button listener for
                                 * achieving this business question. When the user clicks on
                                 * a building to be located in the map, we call (in
                                 * background) the view model in charge,
                                 * so it can update the buildings preferences
                                 * on the firebase json.
                                 *
                                 */
                                        button.setOnClickListener {

                                            /*
                                     * function call to open google maps location handler
                                     */
                                            val spaceObject = spaces.getJSONObject(spaceKey)
                                            val coordenadas =
                                                spaceObject.getJSONArray("coordenadas")
                                            val latitud = coordenadas.getDouble(0)
                                            val longitud = coordenadas.getDouble(1)
                                            abrirGoogleMaps(latitud, longitud)

                                            /*
                                     * we call the view model here to save or update the
                                     * buildings preferences data in the firebase storage
                                     */
                                            userPrefViewModel.saveOrUpdateData(
                                                userEmail!!,
                                                spaceKey
                                            )
                                        }

                                        /*
                                * <-- BQ 2 -->
                                *
                                * Button listener to show the rating form so the user can rate a selected building
                                *
                                */

                                        buttonCalificar.setOnClickListener {
                                            if (isNetworkAvailable()) {
                                                mostrarFormularioCalificacion(spaceKey)
                                            } else {
                                                AlertDialog.Builder(this@MapsActivity)
                                                    .setTitle("No Internet Connection")
                                                    .setMessage("You can only provide ratings while online.")
                                                    .setPositiveButton("OK") { _, _ -> }
                                                    .show()
                                            }
                                        }


                                        val spaceObject = spaces.getJSONObject(spaceKey)
                                        val coordenadas =
                                            spaceObject.getJSONArray("coordenadas")
                                        val latitud = coordenadas.getDouble(0)
                                        val longitud = coordenadas.getDouble(1)
                                        val distanceInMeters: Float? =
                                            null // No tenemos la distancia aquí
                                        val distanceInKilometers: Float? =
                                            null // No tenemos la distancia aquí
                                        val exTimeMinWal: Float? =
                                            null // No tenemos la distancia aquí
                                        val carTimeMinWal: Float? =
                                            null // No tenemos la distancia aquí
                                        val bikeTimeMinWal: Float? =
                                            null // No tenemos la distancia aquí
                                        infoView.text = obtenerInformacionAdicional(
                                            spaceObject,
                                            distanceInMeters,
                                            distanceInKilometers,
                                            exTimeMinWal,
                                            carTimeMinWal,
                                            bikeTimeMinWal
                                        )
                                        // Define the margins
                                        val params = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            setMargins(100, 10, 100, 10) // left, top, right, bottom
                                        }

                                        infoView.layoutParams = params
                                        linearLayout.addView(infoView)
                                    }
                                })

                        })
                }

            }

        } catch (e: Exception) {
            // Handle exceptions when the JSONObject is not obtained
            Log.d("Data Error", "Couldn't receive the json with the data expected: ${e.localizedMessage}")
            Toast.makeText(this@MapsActivity, "Couldn't receive the json with the data expected: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun removeAllLocations() {
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout_locations)
        
        // deletes the layout and then creates a new one

    }


    private fun mostrarFormularioCalificacion(spaceKey: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_calification)

        val btnEnviar = dialog.findViewById<Button>(R.id.btnEnviar)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
        val comentarioEditText = dialog.findViewById<EditText>(R.id.comentarioEditText)

        /*
           * <-- BQ 2 -->
           *
           * Here we use the rating ViewModel in order to submit the rating of the building
           * to the corresponding firebase storage
           *
         */

        comentarioEditText.filters = arrayOf(InputFilter.LengthFilter(150))

        btnEnviar.setOnClickListener {
            // Obtener la calificación y el comentario del usuario
            val calificacion = ratingBar.rating
            val comentario = comentarioEditText.text.toString()

            // Llamar al método del ViewModel para enviar la calificación a Firebase
            ratingViewModel.enviarCalificacionALaBaseDeDatos(spaceKey, calificacion, comentario)

            // Mostrar la notificación al usuario
            mostrarNotificacion()

            // Cerrar el diálogo después de enviar la calificación
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarNotificacion() {
        Toast.makeText(this, "Thanks for sharing your opinion with us!", Toast.LENGTH_SHORT).show()
    }

}

