package com.example.ventura.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.R
import com.example.ventura.viewmodel.JsonViewModel
import com.example.ventura.viewmodel.UserPreferencesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Recuperar el correo del usuario de los extras del intent
        val userEmail = intent.getStringExtra("user_email")


        Log.d("MainMenuActivity", "$userEmail")

        jsonViewModel = ViewModelProvider(this).get(JsonViewModel::class.java)
        userPrefViewModel = ViewModelProvider(this).get(UserPreferencesViewModel::class.java)

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
            for (spaceKey in spaces.keys()) {
                val textLayout = LinearLayout(this)
                textLayout.orientation = LinearLayout.HORIZONTAL
                textLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textLayout.setPadding(16)

                val layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                layoutParams.gravity = Gravity.CENTER_VERTICAL

                val textView = TextView(this)
                textView.text = spaceKey
                textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                textView.setTypeface(Typeface.create("Lato-Light", Typeface.NORMAL))
                textView.layoutParams = layoutParams

                val verMas = TextView(this)
                verMas.text = "Ver más"
                verMas.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                verMas.setTypeface(Typeface.create("Lato-Light", Typeface.NORMAL))

                val button = Button(this)
                button.text = "Ubicar"

                val infoView = TextView(this)
                infoView.visibility = View.GONE
                infoView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                infoView.setTypeface(Typeface.create("Lato-Light", Typeface.NORMAL))

                // Agregar sombra a la vista del contenedor
                textLayout.background = ContextCompat.getDrawable(this, R.drawable.container_background)

                verMas.setOnClickListener {
                    if (infoView.visibility == View.VISIBLE) {
                        infoView.visibility = View.GONE
                        verMas.text = "Ver más"
                    } else {
                        infoView.visibility = View.VISIBLE
                        verMas.text = "Ver menos"

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

                textLayout.addView(textView)
                textLayout.addView(button)
                textLayout.addView(verMas)
                linearLayout.addView(textLayout)
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
        }

    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
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

        // Verificar si las coordenadas del usuario y del edificio están disponibles
        val userCoordsString = if (userCoordinates != null) {
            "Coordenadas del usuario: ${userCoordinates!!.first}, ${userCoordinates!!.second}\n"
        } else {
            "Coordenadas del usuario: Desconocidas\n"
        }

        val buildingCoordsString = if (buildingCoordinates != null) {
            "Coordenadas del edificio: ${buildingCoordinates!!.first}, ${buildingCoordinates!!.second}\n"
        } else {
            "Coordenadas del edificio: Desconocidas\n"
        }

        val distanceString = if (distanceInMeters != null && distanceInKilometers != null) {
            "Distancia al edificio: $distanceInMeters metros (${String.format("%.2f", distanceInKilometers)} kilómetros)\n"
        } else {
            "Distancia al edificio: Desconocida\n"
        }

        val timeWakMin = if (distanceInMeters != null && distanceInKilometers != null) {
            "Tiempo estimado caminando: ${String.format("%.2f", timeWak)} minutos)\n"
        } else {
            "Tiempo estimado caminando: Desconocido\n"
        }

        val timeCarMin = if (distanceInMeters != null && distanceInKilometers != null) {
            "Tiempo estimado en carro: ${String.format("%.2f", timeCar)} minutos)\n"
        } else {
            "Tiempo estimado en carro: Desconocido\n"
        }

        val timeBikeMin = if (distanceInMeters != null && distanceInKilometers != null) {
            "Tiempo estimado en bike: ${String.format("%.2f", timeBike)} minutos)\n"
        } else {
            "Tiempo estimado en bike: Desconocido\n"
        }

        return "Cantidad de pisos: $cantidadPisos \nCantidad de restaurantes: $cantidadRestaurantes \n" +
                "Obstrucciones: $obstrucciones \nCantidad de zonas verdes: $cantidadZonasVerdes\n$userCoordsString$buildingCoordsString$distanceString" +
                timeWakMin + timeCarMin + timeBikeMin
    }


}
