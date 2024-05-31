package com.example.ventura.service

import com.example.ventura.data.remote.BuildingResponse
import com.example.ventura.data.remote.RouteRequest
import com.example.ventura.data.remote.RouteResponse
import com.example.ventura.data.remote.SiteResponse
import com.example.ventura.data.remote.UniversityResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HighPrecisionRouteService {

    @GET("hprouting/university/{id}")
    suspend fun getUniversityById(
        @Path("id") id: Int
    ): UniversityResponse


    @GET("hprouting/building/{id}")
    suspend fun getBuildingById(
        @Path("id") id: String
    ): BuildingResponse


    @GET("hprouting/site/{id}")
    suspend fun getSiteById(
        @Path("id") id: String
    ): SiteResponse


    @GET("hprouting/university/{id}/buildings")
    suspend fun getBuildingsByUniversity(
        @Path("id") id: Int
    ): List<BuildingResponse>


    @GET("hprouting/building/{id}/sites")
    suspend fun getSitesByBuilding(
        @Path("id") id: String
    ): List<SiteResponse>


    @POST("hprouting/shortest-route-between-sites/")
    suspend fun getShortestRouteBetweenSites(
        @Body routeRequest: RouteRequest
    ): RouteResponse


    @GET("{mediaUrl}")
    suspend fun getSiteImage(
        @Path("mediaUrl") mediaUrl: String
    ): Response<ResponseBody>
}
