package com.example.ventura.model

import kotlin.math.pow
import kotlin.math.sqrt

class Location {
    val lat: Double = 0.0
    val lon: Double = 0.0
    // maximum distance which is still considered the same spot for weather purposes
    val tolerance: Double = 0.03


    fun euclideanDistance(b: Location): Double {
        return sqrt((this.lat - b.lat).pow(2.0) + (this.lon - b.lon).pow(2.0))
    }


    @Override
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Location) return false

        val distance = this.euclideanDistance(other)
        // close enough
        return (distance < tolerance)
    }
}