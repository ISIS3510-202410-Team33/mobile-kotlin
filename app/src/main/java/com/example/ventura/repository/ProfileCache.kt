package com.example.ventura.repository

import android.content.Context
import android.util.Log
import com.example.ventura.data.models.Profile


private val TAG = "ProfileCache"

/**
 * Implementation of the ProfileRepository by using the local caching strategy
 */
class ProfileCache(context: Context) : ProfileRepository {

    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    // TODO: fix if cache is erased
    override suspend fun getProfileData(email: String): Profile {
        Log.d(TAG, "Getting profile data from caché")
        return Profile(
            id = sharedPreferences.getInt("id", noneProfile.id),
            name = sharedPreferences.getString(
                "name", noneProfile.name)!!,
            email = sharedPreferences.getString(
                "email", noneProfile.email)!!,
            universityName = sharedPreferences.getString(
                "universityName", noneProfile.universityName)!!
        )
    }

    // TODO: fix if cache is erased
    override suspend fun updateProfileData(id: Int, newProfile: Profile) {
        Log.d(TAG, "Updating profile data to caché")
        with (sharedPreferences.edit()) {
            putString("name", newProfile.name)
            putString("email", newProfile.email)
            putString("universityName", newProfile.universityName)
            // TODO: implement with commit for ACID. Must create coroutine
            apply()
        }
    }

}