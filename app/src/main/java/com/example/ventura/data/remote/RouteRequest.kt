package com.example.ventura.data.remote


/**
 * Represents the JSON body of the request for a short route
 */
data class RouteRequest (
    val site_from_id: String = "",
    val site_to_id: String = "",
    val university_id: Int = -1
)
