package com.example.ventura.repository

import android.util.Log
import com.example.ventura.data.models.Profile
import com.example.ventura.service.ProfileService
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Retrofit

/**
 * Implementation of the ProfileRepository by using the remote, deployed database API
 */
class ProfileRemote (
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