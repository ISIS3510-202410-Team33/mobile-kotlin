package com.example.ventura.model

import android.app.Application
import android.content.Context.MODE_PRIVATE
import com.example.ventura.data.models.Building
import com.example.ventura.data.models.Site
import com.example.ventura.data.remote.RouteResponse
import com.example.ventura.repository.HighPrecisionRouteRepository


private val MONSTERS_UNIVERSITY = 3

class HighPrecisionRouteModel(
    application: Application
) {
    private val sharedPreferences = application.getSharedPreferences("MyPrefs", MODE_PRIVATE)
    private val highPrecisionRouteRepository = HighPrecisionRouteRepository()


    /**
     * Returns the saved university ID
     */
    suspend fun getUniversityId(): Int {
        return sharedPreferences.getInt("university", MONSTERS_UNIVERSITY)
    }


    /**
     * Returns the buildings by the user's default university
     */
    suspend fun getBuildingsByUniversity(): List<Building> {
        return highPrecisionRouteRepository.getBuildingsByUniversity(
            sharedPreferences.getInt("university", MONSTERS_UNIVERSITY)
        )
    }


    /**
     * Returns the sites by the user's chosen building ID
     */
    suspend fun getSitesByBuilding(buildingId: String): List<Site> {
        return highPrecisionRouteRepository.getSitesByBuilding(buildingId)
    }


    /**
     * Returns the direct response for the shortest path between two sites in the university
     */
    suspend fun getShortestRouteBetweenSites(
        siteFromId: String,
        siteToId: String,
        universityId: Int
    ): RouteResponse {
        return highPrecisionRouteRepository.getShortestRouteBetweenSites(
            siteFromId,
            siteToId,
            universityId
        )
    }


    /**
     * Returns the image associated with the URL
     */
    suspend fun getSiteImage(mediaUrl: String): ByteArray? {
        return highPrecisionRouteRepository.getSiteImage(mediaUrl)
    }
}