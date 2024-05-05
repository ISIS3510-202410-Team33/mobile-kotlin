package com.example.ventura.data.remote

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val college: Int,
    // awful correction to improper database construction in the model
    val detail: String
)
