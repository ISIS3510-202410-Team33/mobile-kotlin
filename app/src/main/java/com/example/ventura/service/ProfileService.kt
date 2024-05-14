package com.example.ventura.service

import com.example.ventura.data.remote.CollegeResponse
import com.example.ventura.data.remote.UserResponse
import com.example.ventura.data.remote.UserUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileService {

    @GET("api/users/{id}/")
    suspend fun getUserById(
        @Path("id") id: String
    ): UserResponse


    @GET("api/users")
    suspend fun getUserByEmail(
        @Query("email") email: String
    ): List <UserResponse>


    @GET("api/colleges/{id}/")
    suspend fun getUniversityById(
        @Path("id") id: String
    ): CollegeResponse


    @PUT("api/users/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userUpdateRequest: UserUpdateRequest
    ): UserResponse
}