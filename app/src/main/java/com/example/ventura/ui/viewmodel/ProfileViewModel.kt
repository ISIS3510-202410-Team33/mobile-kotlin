package com.example.ventura.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ventura.data.models.Profile
import com.example.ventura.model.ProfileModel
import com.example.ventura.utils.NetworkHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private val TAG = "ProfileViewModel"

/**
 * Data class for the profile
 */
data class ProfileUiState(
    val profile: Profile = Profile(),
    val putInternetFail: Boolean = false,
    val getInternetFail: Boolean = false
)

class ProfileViewModelFactory(
    private val application: Application,
    private val networkHandler: NetworkHandler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application, networkHandler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



/**
 * ViewModel for the Profile info. Contains all important
 * user info displayed on the ProfileScreen
 */
class ProfileViewModel(
    application: Application,
    private val networkHandler: NetworkHandler
) : ViewModel() {
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
        Log.d(TAG, "Refreshing profile data")

        viewModelScope.launch {
            _uiState.value = ProfileUiState(
                profile = profileModel.getProfileData(
                    fromRemote = networkHandler.isInternetAvailable()
                ),
                getInternetFail = !networkHandler.isInternetAvailable(),
            )
        }
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


    fun updateProfile(
        toRemote: Boolean = false
    ) {
        Log.d(TAG, "Cached profile data updated")
        viewModelScope.launch {
            profileModel.updateProfileData(
                newProfile = uiState.value.profile,
                toCache = true,
                toRemote = toRemote && networkHandler.isInternetAvailable()
            )

            _uiState.update { currentState ->
                currentState.copy(
                    putInternetFail = toRemote && !networkHandler.isInternetAvailable()
                )
            }
        }
    }


    fun updateProfileRemote() {
        Log.d(TAG, "Remote profile data updated")
        viewModelScope.launch {
            profileModel.updateProfileData(
                newProfile = uiState.value.profile,
                toCache = true,
                toRemote = true
            )
        }
    }


    fun checkInternetStatus(): Boolean {
        return networkHandler.isInternetAvailable()
    }
}