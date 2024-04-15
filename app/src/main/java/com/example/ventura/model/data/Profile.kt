package com.example.ventura.model.data


/**
 * Represents the profile of a user in the app
 * @param profileImage resource ID of the users profile image
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


/**
 * Repository for the Profile class. Abstracts
 * model aspects of Profile and fetches from back
 */
class ProfileRepository {
    fun getProfileData(): Profile {
        // TODO: fetch profile data
        return Profile(
            name = "John Doe",
            email = "john.doe@university.com",
            universityName = "Monsters University"
        )
    }
}