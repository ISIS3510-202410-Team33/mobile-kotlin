package com.example.ventura.data.models

/**
 * Represents the profile of a user in the app
 * @param name name of the person
 * @param email email of the user in the form AAAA@mail.ext
 * @param universityName full name of the university
 */
data class Profile(
//    val profileImage: Int,
    val name: String,
    val email: String,
    val universityName: String
)
