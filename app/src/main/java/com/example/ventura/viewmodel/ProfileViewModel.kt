package com.example.ventura.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ventura.model.data.Profile
import com.example.ventura.model.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


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


    /**
     * At start it refreshes data
     */
    init {
        refreshProfileData()
    }


    fun refreshProfileData() {
        _uiState.value = ProfileUiState(profile = profileRepository.getProfileData())
    }


    /**
     * Updates the visual profile's name
     * @param newName the new name of the profile
     */
    fun changeProfileName(newName: String) {
        Log.d("change-profile", "Changing to $newName")
        _uiState.update { currentState ->
            currentState.copy(
                profile = currentState.profile.copy(name = newName)
            )
        }
    }



    /**
     * Updates the visual profile's email
     * @param newEmail the new email of the profile
     */
    fun changeProfileEmail(newEmail: String) {
        Log.d("change-profile", "Changing to $newEmail")
        _uiState.update { currentState ->
            currentState.copy(
                profile = currentState.profile.copy(email = newEmail)
            )
        }
    }


    /**
     * Updates the visual university name of the profile
     * @param newUniversity the new universiy's name of the profile
     */
    fun changeProfileUniversity(newUniversity: String) {
        Log.d("change-profile", "Changing to $newUniversity")
        _uiState.update { currentState ->
            currentState.copy(
                profile = currentState.profile.copy(name = newUniversity)
            )
        }
    }


    fun updateProfileData() {
        Log.d("profile-vm", "Profile data updated")
        profileRepository.updateProfileData(uiState.value.profile)
    }
}