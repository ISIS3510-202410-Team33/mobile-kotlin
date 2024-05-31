package com.example.ventura.model

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.ventura.data.models.Profile
import com.example.ventura.repository.ProfileCache
import com.example.ventura.repository.ProfileRemote


private val TAG = "ProfileModel"

class ProfileModel(application: Application) {
    private val profileCache = ProfileCache(application)
    private val profileRemote = ProfileRemote(application)
    private val sharedPreferences = application.getSharedPreferences("MyPrefs", MODE_PRIVATE)


    suspend fun setFirstTime() {
        val profile = profileRemote.getProfileData(
            sharedPreferences.getString("email", "sullivan@mu.edu")!!
        )

        with (sharedPreferences.edit()) {
            putInt("id", profile.id)
            putString("name", profile.name)
            putString("universityName", profile.universityName)
        }
    }


    /**
     * Returns current profile data from cache
     */
    suspend fun getProfileDataFromCache(): Profile {
        Log.d(TAG, "Getting profile from cache")
        return  profileCache.getProfileData("")
    }

    /**
     * Returns current profile data from remote and updates the caché
     */
    suspend fun getProfileDataFromRemote(): Profile {
        Log.d(TAG, "Getting profile from network on caché")

        val profile = profileRemote.getProfileData(
            sharedPreferences.getString("loginId", "1")!!
        )

        // caché data updated
        updateProfileData(profile, toCache = true, toRemote = false)

        return profileRemote.noneProfile
    }


    /**
     * Returns current profile data
     * Strategy implies fetching from remote and saving on cache
     */
    suspend fun getProfileData(fromRemote: Boolean = true): Profile {
        Log.d(TAG, "Get profile data. From remote = $fromRemote")
        var profile: Profile? = null

        // remote fetch
        if (fromRemote) {
            profile = profileRemote.getProfileData(
                email = sharedPreferences.getString("email", "89")!!
            )
        }

        // cache update
        if (profile != null) updateProfileData(
            newProfile = profile,
            toCache = true,
            toRemote = false
        )
        // cache fetch
        else {
            profile = profileCache.getProfileData(
                email = sharedPreferences.getString("email", "89")!!
            )
        }

        return profile
    }


    suspend fun updateProfileData(
        newProfile: Profile,
        toCache: Boolean = true,
        toRemote: Boolean = false
    ) {
        Log.d(TAG, "Update profile data. To cache? $toCache . To remote? $toRemote")
        if (toCache) profileCache.updateProfileData(newProfile.id, newProfile)
        if (toRemote) profileRemote.updateProfileData(newProfile.id, newProfile)
    }


    /**
     * Updates profile data with the current profile to the cache
     */
    suspend fun updateProfileDataToCache(newProfile: Profile): Unit {
        Log.d(TAG, "Updating profile data to cache")
        profileCache.updateProfileData(newProfile.id, newProfile)
    }


    /**
     * Updates profile data from the current cache to the remote repository
     */
    suspend fun updateProfileDataToRemote(): Unit {
        Log.d("profile-model", "Updating profile data to remote from caché")
        val locallySavedProfile = profileCache.getProfileData("")
        profileRemote.updateProfileData(
            id = sharedPreferences.getInt("id", 89),
            newProfile = locallySavedProfile
        )
    }
}