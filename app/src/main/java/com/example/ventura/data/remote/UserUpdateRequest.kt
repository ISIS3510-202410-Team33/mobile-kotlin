package com.example.ventura.data.remote

data class UserUpdateRequest(
    val name: String,
    val email: String,
    val college: Int
)