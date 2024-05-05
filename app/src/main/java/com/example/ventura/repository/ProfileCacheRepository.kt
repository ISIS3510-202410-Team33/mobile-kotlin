package com.example.ventura.repository

import com.example.ventura.data.models.Profile

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
