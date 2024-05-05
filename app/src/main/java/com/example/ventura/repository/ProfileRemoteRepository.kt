package com.example.ventura.repository

import com.example.ventura.data.models.Profile

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
