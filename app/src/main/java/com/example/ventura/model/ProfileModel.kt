package com.example.ventura.model

import android.app.Application
import com.example.ventura.model.data.Profile
import com.example.ventura.model.data.ProfileCache

class ProfileModel(application: Application) {
    private val profileCache = ProfileCache(application)
//    private val profileRemote = ProfileRemote(
//        backendUrl = "https://ventura-backend-jaj1.onrender.com",
//        email = "None"
//    )

    /**
     * Returns current profile data
     */
    fun getProfileData(): Profile {
        // from cache
        var profile = profileCache.getProfileData()

        if (profile == profileCache.noneProfile) {
            // from network
            //profile = profileRemote.getProfileData()
        }

        return profile
    }


    /**
     * Updates profile data with the
     * current profile set
     */
    fun updateProfileData(newProfile: Profile): Unit {
        profileCache.updateProfileData(newProfile)
    }
}