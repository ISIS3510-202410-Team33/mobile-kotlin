package com.example.ventura.model.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log


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



/**
 * Repository for the Profile class. Abstracts
 * model aspects of Profile and fetches from back
 */
interface ProfileRepository {
    val noneProfile: Profile
        get() = Profile(
            name = "Unavailable",
            email = "Unavailable",
            universityName = "Unavailable"
        )

    /**
     * Returns profile data for the current logged user
     * @return Profile object with current user info
     */
    fun getProfileData(): Profile

    /**
     * Updates profile data for the profile given
     * @param newProfile new profile object with information to update
     */
    fun updateProfileData(newProfile: Profile)
}


/**
 * Implementation of the ProfileRepository by using the local caching strategy
 */
class ProfileCache(context: Context) : ProfileRepository {

    val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)

    override fun getProfileData(): Profile {
        Log.d("profile-cache", "Getting profile data from caché")
        return Profile(
            name = sharedPreferences.getString(
                "name", noneProfile.name)!!,
            email = sharedPreferences.getString(
                "email", noneProfile.email)!!,
            universityName = sharedPreferences.getString(
                "universityName", noneProfile.universityName)!!
        )
    }

    override fun updateProfileData(newProfile: Profile) {
        Log.d("profile-cache", "Updating profile data to caché")
        with (sharedPreferences.edit()) {
            putString("name", newProfile.name)
            putString("email", newProfile.email)
            putString("universityName", newProfile.universityName)
            // TODO: implement with commit for ACID. Must create coroutine
            apply()
        }
    }

}

/**
 * Implementation of the ProfileRepository by using the remote, deployed database API
 */
//class ProfileRemote(
//    private val backendUrl: String,
//    private val email: String
//) : ProfileRepository {
//
//    // firebase authenticator. Used to get user info
//    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
//
//    // to make backend requests
//    private val retrofit = Retrofit.Builder()
//        .baseUrl(backendUrl)
//        .build()
//
//    private val profileService: ProfileService = retrofit.create(ProfileService::class.java)
//
//
//    override fun getProfileData(): Profile {
//        val userResponse = profileService.getUserByEmail(email)
//        val collegeResponse = profileService.getUniversityById(userResponse.college)
//
//        return Profile(
//            name = userResponse.name,
//            email = userResponse.email,
//            universityName = collegeResponse.name
//        )
//    }
//
//
//    override fun updateProfileData(newProfile: Profile) {
//        profileService.updateUser(
//            email = email,
//            newName = newProfile.name
//        )
//    }
//}