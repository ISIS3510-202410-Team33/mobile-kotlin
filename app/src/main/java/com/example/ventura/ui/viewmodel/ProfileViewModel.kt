package com.example.ventura.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ventura.data.models.Profile
import com.example.ventura.model.ProfileModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Data class for the profile
 */
data class ProfileUiState(
    val profile: Profile = Profile("","","")
)

class ProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



/**
 * ViewModel for the Profile info. Contains all important
 * user info displayed on the ProfileScreen
 */
class ProfileViewModel(application: Application) : ViewModel() {
    private val profileModel = ProfileModel(application)

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
        Log.d("profile-vm", "Refreshing profile data")
        _uiState.value = ProfileUiState(profile = profileModel.getProfileDataFromCache())

//        viewModelScope.launch(Dispatchers.Default) {
//            Log.d("profile-vm", "Retrieved remote profile is ")
//            val remoteFetchedProfile = profileModel.getProfileDataFromRemote()
//            _uiState.value = ProfileUiState(profile = remoteFetchedProfile)
//        }
    }


    /**
     * Updates the visual profile's name
     * @param newName the new name of the profile
     */
    fun changeProfileName(newName: String) {
        Log.d("profile-vm", "Changing display name to $newName")
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
        Log.d("profile-vm", "Changing display email to $newEmail")
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
        Log.d("profile-vm", "Changing universityName to $newUniversity")
        _uiState.update { currentState ->
            currentState.copy(
                profile = currentState.profile.copy(name = newUniversity)
            )
        }
    }


    fun updateProfileCache() {
        Log.d("profile-vm", "Profile data updated")
        profileModel.updateProfileDataToCache(uiState.value.profile)
    }


    fun updateProfileRemote() {
        Log.d("profile-vm", "Profile data updated remotely")
        viewModelScope.launch {
            profileModel.updateProfileDataToRemote()
        }
    }
}