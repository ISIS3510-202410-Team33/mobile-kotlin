package com.example.ventura.data.remote

data class RouteResponse(
    val sites: List<SiteResponse> = emptyList(),
    val distance: Int = -1
)
