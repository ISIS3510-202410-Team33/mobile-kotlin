package com.example.ventura.model.service

import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val college: Int
)

data class CollegeResponse(
    val id: Int,
    val name: String
)



interface ProfileService {

    @GET("api/users/?email={email}")
    suspend fun getUserByEmail(
        @Path("email") email: String
    ): UserResponse


    @GET("api/colleges/{id}")
    suspend fun getUniversityById(
        @Path("id") id: Int
    ): CollegeResponse


    @PUT("api/users/?email={email}")
    suspend fun updateUser(
        @Path("email") email: String,
        @Field("name") newName: String
    )
}