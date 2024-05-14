package com.example.ventura.repository

import com.example.ventura.data.models.Building
import com.example.ventura.data.models.Site
import com.example.ventura.data.models.University
import com.example.ventura.data.remote.BuildingResponse
import com.example.ventura.data.remote.RouteRequest
import com.example.ventura.data.remote.RouteResponse
import com.example.ventura.data.remote.SiteResponse
import com.example.ventura.data.remote.UniversityResponse
import com.example.ventura.service.HighPrecisionRouteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val BACKEND_URL = "http://192.168.1.187:42069/"


class HighPrecisionRouteRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BACKEND_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val hprService = retrofit.create(HighPrecisionRouteService::class.java)


    suspend fun getUniversity(universityId: Int): University {
        // uses empty university response to avoid NullPointer Exception
        var university: UniversityResponse = UniversityResponse()

        withContext (Dispatchers.IO) {
            university = hprService.getUniversityById(universityId)
        }

        return University (
            id = university.id,
            name = university.name
        )
    }


    suspend fun getBuilding(buildingId: String): Building {
        var building: BuildingResponse = BuildingResponse()
        var university: University = University()

        withContext (Dispatchers.IO) {
            building = hprService.getBuildingById(buildingId)
            // tries to retrieve the university by the response's university ID
            university = getUniversity(building.university)

        }

        return Building(
            id = building.id,
            name = building.name,
            university = university
        )
    }


    suspend fun getSite(siteId: String): Site {
        var site: SiteResponse = SiteResponse()
        var building: Building = Building()

        withContext (Dispatchers.IO) {
            site = hprService.getSiteById(siteId)
            // retrieve building by response's building ID
            building = getBuilding(site.building)
        }

        return Site(
            id = site.id,
            name = site.id,
            imgUrl = site.img,
            type = site.type,
            building = building
        )
    }


    suspend fun getBuildingsByUniversity(universityId: Int): List<Building> {
        var buildings: List<BuildingResponse> = emptyList()
        var university: University = University()

        withContext (Dispatchers.IO) {
            buildings = hprService.getBuildingsByUniversity(universityId)
            university = getUniversity(universityId)
        }

        return buildings.map {
            Building(
                id = it.id,
                name = it.name,
                university = university
            )
        }
    }


    suspend fun getSitesByBuilding(buildingId: String): List<Site> {
        var sites: List<SiteResponse> = emptyList()
        var building: Building = Building()

        withContext (Dispatchers.IO) {
            sites = hprService.getSitesByBuilding(buildingId)
            building = getBuilding(buildingId)
        }

        return sites.map {
            Site (
                id = it.id,
                name = it.name,
                imgUrl = it.img,
                type = it.type,
                building = building
            )
        }
    }


    /**
     * Gets the shortest route between two sites. In order to make the operation fast,
     * it returns the response object. The VM will pull the other parts as needed.
     */
    suspend fun getShortestRouteBetweenSites(
        siteFromId: String,
        siteToId: String,
        universityId: Int
    ): RouteResponse {
        return hprService.getShortestRouteBetweenSites(
            RouteRequest(
                site_from_id = siteFromId,
                site_to_id = siteToId,
                university_id = universityId
            )
        )
    }


    /**
     * Returns the image associated with the URL as a ByteArray
     */
    suspend fun getSiteImage(mediaUrl: String): ByteArray? {
        val response = hprService.getSiteImage(mediaUrl)
        return if (response.isSuccessful) {
            response.body()!!.bytes()
        } else null
    }
}