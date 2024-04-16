package com.example.ventura.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ventura.model.data.Profile
import com.example.ventura.model.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Data class for the profile
 */
data class ProfileUiState(
    val profile: Profile = Profile("","","")
)


/**
 * ViewModel for the Profile info. Contains all important
 * user info displayed on the ProfileScreen
 */
class ProfileViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()

    // inner object, modifiable by the ViewModel
    private val _uiState = MutableStateFlow(ProfileUiState())
    // returned, only viewable object for the View
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfileData()
    }


    fun refreshProfileData() {
        _uiState.value = ProfileUiState(profile = profileRepository.getProfileData())
    }
}