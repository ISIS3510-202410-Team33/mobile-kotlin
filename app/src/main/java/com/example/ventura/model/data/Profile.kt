package com.example.ventura.model.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.ventura.model.service.ProfileService
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Retrofit


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

interface ProfileRepository {
    val noneProfile: Profile
        get() = Profile(
            name = "Guest",
            email = "Unavailable",
            universityName = "Unavailable"
        )
}



/**
 * Repository for the Profile class. Abstracts
 * model aspects of Profile and fetches from caching structures. Doesn't require coroutines
 */
interface ProfileCacheRepository : ProfileRepository {


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
 * Repository for the Profile class. Abstracts
 * model aspects of Profile and fetches from backend. Separated due to coroutine obligations
 */
interface ProfileRemoteRepository : ProfileRepository {

    /**
     * Returns profile data for the current logged user
     * @param id profile identifier
     * @return Profile object with current user info
     */
    suspend fun getProfileData(id: String): Profile

    /**
     * Updates profile data for the profile given
     * @param id profile identifier
     * @param newProfile new profile object with information to update
     */
    suspend fun updateProfileData(id: String, newProfile: Profile)
}


/**
 * Implementation of the ProfileRepository by using the local caching strategy
 */
class ProfileCache(context: Context) : ProfileCacheRepository {

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
class ProfileBackendRepository (
    backendUrl: String
) : ProfileRemoteRepository {

    // firebase authenticator. Used to get user info
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // to make backend requests
    private val retrofit = Retrofit.Builder()
        .baseUrl(backendUrl)
        .build()

    private val profileService: ProfileService = retrofit.create(ProfileService::class.java)


    /**
     * @param id email of the user
     */
    override suspend fun getProfileData(id: String): Profile {
        // TODO: check that it returns
        val userResponse = profileService.getUserById(id)
        Log.e("profile-remrepo", userResponse.detail)
        val collegeResponse = profileService.getUniversityById(userResponse.college.toString())

        return Profile(
            name = userResponse.name,
            email = userResponse.email,
            universityName = collegeResponse.name
        )
    }


    /**
     * @param id email of the user
     */
    override suspend fun updateProfileData(id: String, newProfile: Profile) {
        profileService.updateUser(
            id = id,
            newName = newProfile.name
            // TODO: change university
        )
    }
}