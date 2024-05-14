package com.example.ventura.repository

import com.example.ventura.data.models.Profile

interface ProfileRepository {
    val noneProfile: Profile
        get() = Profile(
            id = -1,
            name = "Guest",
            email = "Unavailable",
            universityName = "Unavailable"
        )

    /**
     * Returns profile data for the current logged user
     * @param id profile identifier (email)
     * @return Profile object with current user info
     */
    suspend fun getProfileData(email: String): Profile

    /**
     * Updates profile data for the profile given
     * @param id profile identifier
     * @param newProfile new profile object with information to update
     */
    suspend fun updateProfileData(id: Int, newProfile: Profile)
}
