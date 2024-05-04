package com.example.ventura.repository

import android.content.Context
import android.util.Log
import com.example.ventura.data.models.Profile

/**
 * Implementation of the ProfileRepository by using the local caching strategy
 */
class ProfileCache(context: Context) : ProfileCacheRepository {

    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

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