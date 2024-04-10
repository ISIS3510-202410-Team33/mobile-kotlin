package com.example.ventura.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.R
import com.example.ventura.viewmodel.JsonViewModel
import com.example.ventura.viewmodel.JsonViewModelFactory
import com.example.ventura.viewmodel.RatingViewModel
import com.example.ventura.viewmodel.UserPreferencesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.nio.charset.Charset

class MapsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var jsonOb: JSONObject? = null
    private var lastKnownLocation: Location? = null

    // Variables para almacenar las coordenadas y la distancia
    private var userCoordinates: Pair<Double, Double>? = null
    private var buildingCoordinates: Pair<Double, Double>? = null
    private var distanceToBuilding: Float? = null

    // Average delay in km/h on city
    private var averageWalkingDelay: Float = 5F
    private var averageBikeDelay: Float = 3F
    private var averageCarDelay: Float = 2F

    private lateinit var jsonViewModel: JsonViewModel
    private lateinit var userPrefViewModel: UserPreferencesViewModel
    private lateinit var ratingViewModel: RatingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)


        // Recuperar el correo del usuario de los extras del intent
        val userEmail = intent.getStringExtra("user_email")


        Log.d("MainMenuActivity", "$userEmail")

        jsonViewModel = ViewModelProvider(this, JsonViewModelFactory(this)).get(JsonViewModel::class.java)
        userPrefViewModel = ViewModelProvider(this).get(UserPreferencesViewModel::class.java)
        ratingViewModel = ViewModelProvider(this).get(RatingViewModel::class.java)

        if (!jsonViewModel.isDataAvailableLocally()) {
            Toast.makeText(this, "La información se está obteniendo del servidor, por favor espere...", Toast.LENGTH_LONG).show()
        }

        // Llamar a la función fetchJsonData bloqueando el hilo principal
        runBlocking(Dispatchers.IO) {
            try {
                val jsonObject = jsonViewModel.fetchJsonData()
                // Manejar el JSONObject recibido
                jsonOb = jsonObject


                Log.d("vaina", "llego esta vaiana")
            } catch (e: Exception) {
                // Manejar cualquier error
                Log.d("asd", "no llego esta vaina")
            }
        }

        runOnUiThread {
            Toast.makeText(this, "Información cargada exitosamente", Toast.LENGTH_SHORT).show()


        }


        val buttonBackToMenu = findViewById<Button>(R.id.buttonBackToMenu)
        buttonBackToMenu.setOnClickListener {
            // Crear un intent para regresar al MainMenuActivity
            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("user_email", userEmail) // Aquí pasamos el correo como un extra
            startActivity(intent)
            finish() // Opcional: finaliza la actividad actual para liberar recursos
        }

        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Set the desired interval for active location updates, in milliseconds.
            fastestInterval = 5000 // Set the fastest rate for active location updates, in milliseconds.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority of the request.
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // Call a method to handle the current location.

                    lastKnownLocation = location // Guardar la última ubicación conocida
                    userCoordinates = Pair(location.latitude, location.longitude)
                    Log.d("Location", "$lastKnownLocation")

                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // Permissions are already granted, start location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)

        val spaces = jsonOb?.getJSONObject("spaces")

        if (spaces != null) {

            val obtainedRecommendationsLiveData = MutableLiveData<List<String>>()

            // create recomendations
            userPrefViewModel.getData(userEmail).observe(this, Observer { jsonObject: JSONObject ->
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
                val obtainedRecommendations = generateRecommendations(maxEntry.first, spaces)
                Log.d("FORMAT RECOMENDATION - ALL", obtainedRecommendations.toString())

                // Update the LiveData variable
                obtainedRecommendationsLiveData.postValue(obtainedRecommendations)
            })

            ratingViewModel.obtenerEdificioConMejorPuntaje().observe(this, Observer { mejorEdificio ->
                // Log the result
                Log.d("YourOtherActivity", "Mejor edificio: $mejorEdificio")

            // Observe the Live Data to get updates
            obtainedRecommendationsLiveData.observe(this, Observer { recommendations ->

                for (spaceKey in spaces.keys()) {
                    val textLayout = LinearLayout(this)
                    textLayout.orientation = LinearLayout.VERTICAL // Change orientation to vertical
                    textLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textLayout.setPadding(16)

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // Adjust width to match parent
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.CENTER_VERTICAL

                    val textView = TextView(this)
                    textView.text = spaceKey
                    textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                    textView.setTypeface(Typeface.create("Lato-Light", Typeface.NORMAL))
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F) // Set text size to 20sp
                    textView.layoutParams = layoutParams


                    val verMas = TextView(this)
                    verMas.text = "View more information"
                    verMas.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F) // Set text size to 20sp
                    verMas.setTextColor(Color.parseColor("#d0d4f5"));
                    verMas.setTypeface(Typeface.create("Lato-Light", Typeface.BOLD))

                    val button = Button(this)
                    button.text = "Locate in map"
                    button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#363c45")) // Set button background color to red

                    val buttonCalificar = TextView(this)
                    buttonCalificar.text = "Rate this location!"
                    buttonCalificar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F) // Set text size to 20sp
                    buttonCalificar.setTextColor(Color.parseColor("#ddf5b8"));
                    buttonCalificar.setTypeface(Typeface.create("Lato-Light", Typeface.BOLD))


                    val infoView = TextView(this)
                    infoView.visibility = View.GONE
                    infoView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                    infoView.setTypeface(Typeface.create("Lato-Light", Typeface.NORMAL))

                    val isRecommended = recommendations.contains(spaceKey)
                    val isBestRated = mejorEdificio.contains(spaceKey)

                    if (isRecommended) {
                        val recommendedMessage = TextView(this)
                        recommendedMessage.text = "Recommended location!"
                        recommendedMessage.setTextColor(0xE3B77800.toInt()) // Set text color to red
                        recommendedMessage.setTypeface(Typeface.DEFAULT_BOLD)
                        textLayout.addView(recommendedMessage) // Add recommended message here
                    }

                    if (isBestRated) {
                        val rateddedMessage = TextView(this)
                        rateddedMessage.text = "Best Rated location!"
                        rateddedMessage.setTextColor(0xE3B77800.toInt()) // Set text color to red
                        rateddedMessage.setTypeface(Typeface.DEFAULT_BOLD)
                        textLayout.addView(rateddedMessage) // Add recommended message here
                    }



                    textLayout.addView(textView)
                    textLayout.addView(button)
                    textLayout.addView(verMas)
                    textLayout.addView(buttonCalificar)
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
                            val coordenadasEdificio = spaceObject.getJSONArray("coordenadas")
                            val latitudEdificio = coordenadasEdificio.getDouble(0)
                            val longitudEdificio = coordenadasEdificio.getDouble(1)
                            buildingCoordinates = Pair(latitudEdificio, longitudEdificio)

                            // Calcular la distancia entre las coordenadas del usuario y las del edificio
                            userCoordinates?.let { userCoords ->
                                buildingCoordinates?.let { buildingCoords ->
                                    val results = FloatArray(1)
                                    Location.distanceBetween(userCoords.first, userCoords.second, buildingCoords.first, buildingCoords.second, results)
                                    distanceToBuilding = results[0]

                                    // Convertir la distancia a metros y kilómetros
                                    val distanceInMeters = distanceToBuilding
                                    val distanceInKilometers = distanceToBuilding?.div(1000)
                                    val wakExpectedTimeMin = distanceInKilometers?.times(averageWalkingDelay)
                                    val carExpectedTimeMin = distanceInKilometers?.times(averageCarDelay)
                                    val bikeExpectedTimeMin = distanceInKilometers?.times(averageBikeDelay)

                                    // Actualizar la información adicional
                                    val infoText = obtenerInformacionAdicional(spaceObject, distanceInMeters, distanceInKilometers, wakExpectedTimeMin, carExpectedTimeMin, bikeExpectedTimeMin)
                                    infoView.text = infoText

                                }
                            }


                        }
                    }


                    button.setOnClickListener {
                        val spaceObject = spaces.getJSONObject(spaceKey)
                        val coordenadas = spaceObject.getJSONArray("coordenadas")
                        val latitud = coordenadas.getDouble(0)
                        val longitud = coordenadas.getDouble(1)
                        abrirGoogleMaps(latitud, longitud)

                        // Llamar a la función del ViewModel para guardar o actualizar los datos en Firebase Storage
                        userPrefViewModel.saveOrUpdateData(userEmail!!, spaceKey)
                    }

                    buttonCalificar.setOnClickListener {
                        mostrarFormularioCalificacion(spaceKey)
                    }

                    button.setOnClickListener {
                        val spaceObject = spaces.getJSONObject(spaceKey)
                        val coordenadas = spaceObject.getJSONArray("coordenadas")
                        val latitud = coordenadas.getDouble(0)
                        val longitud = coordenadas.getDouble(1)
                        abrirGoogleMaps(latitud, longitud)

                        // Llamar a la función del ViewModel para guardar o actualizar los datos en Firebase Storage
                        userPrefViewModel.saveOrUpdateData(userEmail!!, spaceKey)
                    }

                    val spaceObject = spaces.getJSONObject(spaceKey)
                    val coordenadas = spaceObject.getJSONArray("coordenadas")
                    val latitud = coordenadas.getDouble(0)
                    val longitud = coordenadas.getDouble(1)
                    val distanceInMeters: Float? = null // No tenemos la distancia aquí
                    val distanceInKilometers: Float? = null // No tenemos la distancia aquí
                    val exTimeMinWal: Float? = null // No tenemos la distancia aquí
                    val carTimeMinWal: Float? = null // No tenemos la distancia aquí
                    val bikeTimeMinWal: Float? = null // No tenemos la distancia aquí
                    infoView.text = obtenerInformacionAdicional(spaceObject, distanceInMeters, distanceInKilometers, exTimeMinWal, carTimeMinWal, bikeTimeMinWal)
                    linearLayout.addView(infoView)
                }
            })

            })
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
        val recommendations = mutableListOf<String>()
        for (spaceKey in spaces.keys()) {
            if (spaceKey.contains(mostVisited)) {
                recommendations.add(spaceKey)
                Log.d("Recommendation", spaceKey)
            }
        }
        return recommendations
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
        }
    }



    private fun obtenerInformacionAdicional(spaceObject: JSONObject, distanceInMeters: Float?, distanceInKilometers: Float?, timeWak: Float?, timeCar: Float?, timeBike: Float?): String {
        val cantidadPisos = spaceObject.getInt("cantidad_pisos")
        val obstrucciones = spaceObject.getBoolean("obstrucciones")
        val cantidadRestaurantes = spaceObject.getInt("cantidad_restaurantes")
        val cantidadZonasVerdes = spaceObject.getInt("cantidad_zonas_verdes")

        /*

                // Verificar si las coordenadas del usuario y del edificio están disponibles
                val userCoordsString = if (userCoordinates != null) {
                    "Your current coordenates: ${userCoordinates!!.first}, ${userCoordinates!!.second}\n"
                } else {
                    "Your current coordenate: Desconocidas\n"
                }

                val buildingCoordsString = if (buildingCoordinates != null) {
                    "Destination coordinates: ${buildingCoordinates!!.first}, ${buildingCoordinates!!.second}\n"
                } else {
                    "Destination coordinates: Desconocidas\n"
                }

                */

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

    private fun mostrarFormularioCalificacion(spaceKey: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_calification)

        val btnEnviar = dialog.findViewById<Button>(R.id.btnEnviar)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
        val comentarioEditText = dialog.findViewById<EditText>(R.id.comentarioEditText)

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
