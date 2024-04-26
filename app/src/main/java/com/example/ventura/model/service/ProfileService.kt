package com.example.ventura.model.service

import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path



data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val college: Int,
    // awful correction to improper database construction in the model
    val detail: String
)

data class CollegeResponse(
    val id: Int,
    val name: String
)



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