package com.example.ventura.model

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.ventura.data.models.Profile
import com.example.ventura.repository.ProfileCache
import com.example.ventura.repository.ProfileRemote

class ProfileModel(application: Application) {
    private val profileCache = ProfileCache(application)
    private val profileRemote = ProfileRemote(
        backendUrl = "https://ventura-backend-jaj1.onrender.com/"
    )
    private val sharedPreferences = application.getSharedPreferences("MyPrefs", MODE_PRIVATE)

    /**
     * Returns current profile data from cache
     */
    fun getProfileDataFromCache(): Profile {
        Log.d("profile-model", "Getting profile from cache")
        return  profileCache.getProfileData()
    }

    /**
     * Returns current profile data from remote and updates the caché
     */
    suspend fun getProfileDataFromRemote(): Profile {
        Log.d("profile-model", "Getting profile from network on caché")

        val profile = profileRemote.getProfileData(
            sharedPreferences.getString("loginId", "1")!!
        )

        // caché data updated
        updateProfileDataToCache(profile)

        return profileRemote.noneProfile
    }


    /**
     * Updates profile data with the current profile to the cache
     */
    fun updateProfileDataToCache(newProfile: Profile): Unit {
        Log.d("profile-model", "Updating profile data to cache")
        profileCache.updateProfileData(newProfile)
    }


    /**
     * Updates profile data from the current cache to the remote repository
     */
    suspend fun updateProfileDataToRemote(): Unit {
        Log.d("profile-model", "Updating profile data to remote from caché")
        val locallySavedProfile = profileCache.getProfileData()
        profileRemote.updateProfileData(
            id = sharedPreferences.getString("loginId", "1")!!,
            newProfile = locallySavedProfile
        )
    }
}