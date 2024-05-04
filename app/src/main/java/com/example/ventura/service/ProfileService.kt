package com.example.ventura.service

import com.example.ventura.data.remote.CollegeResponse
import com.example.ventura.data.remote.UserResponse
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfileService {

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): UserResponse


    @GET("api/colleges/{id}")
    suspend fun getUniversityById(
        @Path("id") id: String
    ): CollegeResponse


    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Field("name") newName: String
    ): UserResponse
}