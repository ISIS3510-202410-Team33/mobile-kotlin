// MapsActivity.kt FUNCIONAL SIN GPS
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.example.ventura.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import kotlin.math.roundToInt

class MapsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var lastKnownLocation: Location? = null
    // Variables para almacenar las coordenadas y la distancia
    private var userCoordinates: Pair<Double, Double>? = null
    private var buildingCoordinates: Pair<Double, Double>? = null
    private var distanceToBuilding: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

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

        val jsonString = "{\n" +
                "    \"spaces\": {\n" +
                "        \"Edificio_ML\": {\n" +
                "            \"coordenadas\": [4.602645428131801, -74.06487758466257],\n" +
                "            \"cantidad_pisos\": 10,\n" +
                "            \"cantidad_restaurantes\": 2,\n" +
                "            \"cantidad_zonas_verdes\":0\n" +
                "        },\n" +
                "        \"Edificio_W\": {\n" +
                "            \"coordenadas\": [4.602322724127535, -74.06502244862835],\n" +
                "            \"cantidad_pisos\": 15,\n" +
                "            \"cantidad_restaurantes\": 3,\n" +
                "            \"cantidad_zonas_verdes\":0\n" +
                "        },\n" +
                "        \"Centro_del_Japon\": {\n" +
                "            \"coordenadas\": [4.60106570194588, -74.06644974069125],\n" +
                "            \"cantidad_pisos\": 20,\n" +
                "            \"cantidad_restaurantes\": 5,\n" +
                "            \"cantidad_zonas_verdes\":0\n" +
                "        }\n" +
                "    }\n" +
                "}"

        val jsonObject = JSONObject(jsonString)
        val spaces = jsonObject.getJSONObject("spaces")

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

                            // Actualizar la información adicional
                            val infoText = obtenerInformacionAdicional(spaceObject, distanceInMeters, distanceInKilometers)
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
            infoView.text = obtenerInformacionAdicional(spaceObject, distanceInMeters, distanceInKilometers)
            linearLayout.addView(infoView)

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

    private fun obtenerInformacionAdicional(spaceObject: JSONObject, distanceInMeters: Float?, distanceInKilometers: Float?): String {
        val cantidadPisos = spaceObject.getInt("cantidad_pisos")
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

        return "Cantidad de pisos: $cantidadPisos\nCantidad de restaurantes: $cantidadRestaurantes\nCantidad de zonas verdes: $cantidadZonasVerdes\n$userCoordsString$buildingCoordsString$distanceString"
    }



    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

}