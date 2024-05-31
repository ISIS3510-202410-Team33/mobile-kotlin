package com.example.ventura.repository

import android.content.Context
import com.example.ventura.BuildConfig
import com.example.ventura.data.models.Profile
import com.example.ventura.data.remote.CollegeResponse
import com.example.ventura.data.remote.UserResponse
import com.example.ventura.data.remote.UserUpdateRequest
import com.example.ventura.service.ProfileService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val TAG = "ProfileRemote"

/**
 * Implementation of the ProfileRepository by using the remote, deployed database API
 */
class ProfileRemote (context: Context) : ProfileRepository {

    // firebase authenticator. Used to get user info
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    // to make backend requests
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.DJANGO_BACKEND_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val profileService: ProfileService = retrofit.create(ProfileService::class.java)


    /**
     * @param id ID of the user
     */
    override suspend fun getProfileData(email: String): Profile {
        var userResponse = UserResponse()
        var collegeResponse = CollegeResponse()

        withContext (Dispatchers.IO) {
            val listUserResponse = profileService.getUserByEmail(email)
            // pulls first and only one
            userResponse = listUserResponse.get(0)
            collegeResponse = profileService.getUniversityById(userResponse.college.toString())
        }

        return Profile(
            id = userResponse.id,
            name = userResponse.name,
            email = userResponse.email,
            universityName = collegeResponse.name
        )
    }


    /**
     * @param id ID of the user
     */
    override suspend fun updateProfileData(id: Int, newProfile: Profile) {
        withContext (Dispatchers.IO) {
            profileService.updateUser(
                id = id,
                userUpdateRequest = UserUpdateRequest(
                    name = newProfile.name,
                    email = newProfile.email,
                    college = sharedPreferences.getInt("university", 3)
                )
            )
        }
    }
}