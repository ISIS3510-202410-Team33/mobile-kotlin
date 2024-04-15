package com.example.ventura.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventura.model.data.Profile
import com.example.ventura.model.data.ProfileRepository


/**
 * ViewModel for the Profile info. Contains all important
 * user info displayed on the ProfileScreen
 */
class ProfileViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()

    // inner object, modifiable by the ViewModel
    private val _profileData = MutableLiveData<Profile>()
    // returned, only viewable object for the View
    val profileData: LiveData<Profile> = _profileData

    fun refreshProfileData() {
        val profile = profileRepository.getProfileData()
        _profileData.value = profile
    }
}