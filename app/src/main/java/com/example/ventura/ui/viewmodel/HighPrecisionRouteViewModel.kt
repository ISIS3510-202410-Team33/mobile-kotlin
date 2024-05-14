package com.example.ventura.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ventura.data.models.Building
import com.example.ventura.data.models.Site
import com.example.ventura.data.remote.RouteResponse
import com.example.ventura.model.HighPrecisionRouteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val TAG = "HighPrecisionRouteViewModel"

data class HighPrecisionRouteUiState(
    val buildings: List <Building> = emptyList(),
    val sitesFrom: List <Site> = emptyList(),
    val sitesTo: List <Site> = emptyList(),
    val selectedFromSite: Site? = null,
    val selectedToSite: Site? = null,
    val route: RouteResponse? = null,
    val currentNodeIndex: Int? = null,
    val currentNodePath: String? = null,
    val currentNodeImage: ByteArray? = null
)


class HighPrecisionRouteViewModelFactory(private val model: HighPrecisionRouteModel): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HighPrecisionRouteViewModel::class.java)) {
            return HighPrecisionRouteViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}


class HighPrecisionRouteViewModel(
    private val highPrecisionRouteModel: HighPrecisionRouteModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(HighPrecisionRouteUiState())

    val uiState: StateFlow <HighPrecisionRouteUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "Fetching university buildings")
        // fetches the buildings
        viewModelScope.launch (Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    buildings = highPrecisionRouteModel.getBuildingsByUniversity()
                )
            }
            Log.d(TAG, "Buildings fetched: ${uiState.value.buildings.size}")
            dropRoute()
        }
    }


    /**
     * Fetches the sites to start from by using the building ID
     * Called when user chooses new starting building
     */
    fun getFromSitesByBuilding(buildingId: String) {
        Log.d(TAG, "Updating from sites by building $buildingId")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    sitesFrom = highPrecisionRouteModel.getSitesByBuilding(buildingId)
                )
            }
            dropRoute()
            unsetFromSite()
        }
    }


    /**
     * Fetches the sites to end at by using the building ID.
     * Called when user chooses new destintation building
     */
    fun getToSitesByBuilding(buildingId: String) {
        Log.d(TAG, "Updating to sites by building $buildingId")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(sitesTo = highPrecisionRouteModel.getSitesByBuilding(buildingId))
            }
            dropRoute()
            unsetToSite()
        }
    }


    fun setFromSite(selectedSite: Site) {
        Log.d(TAG, "Updating starting site")
        _uiState.update {
            it.copy(selectedFromSite = selectedSite)
        }
    }


    private fun unsetFromSite() {
        Log.d(TAG, "Unsetting from site")
        _uiState.update {
            it.copy(selectedFromSite = null)
        }
    }


    fun setToSite(selectedSite: Site) {
        Log.d(TAG, "Updating destination site")
        _uiState.update {
            it.copy(selectedToSite = selectedSite)
        }
    }


    private fun unsetToSite() {
        Log.d(TAG, "Unsetting destination site")
        _uiState.update {
            it.copy(selectedToSite = null)
        }
    }


    /**
     * Certain updates imply dropping the obtained route
     */
    private fun dropRoute() {
        _uiState.update {
            it.copy(
                route = null,
                currentNodeIndex = null,
                currentNodePath = null,
                currentNodeImage = null
            )
        }
    }

    /**
     * Updates the route from the two selected sites
     */
    fun getRouteSites() {
        Log.d(TAG, "Obtaining route")

        viewModelScope.launch (Dispatchers.IO) {
            // gets the route
            _uiState.update {
                it.copy(route = highPrecisionRouteModel.getShortestRouteBetweenSites(
                    siteFromId = uiState.value.selectedFromSite!!.id,
                    siteToId = uiState.value.selectedToSite!!.id,
                    universityId = highPrecisionRouteModel.getUniversityId()
                ))
            }
            Log.d(TAG, "Route updated")

            // checks if route is valid
            if (_uiState.value.route != null && _uiState.value.route!!.sites.isNotEmpty()) {

                // sets starting showing node
                _uiState.update {
                    it.copy(
                        currentNodeIndex = 0,
                    )
                }
                Log.d(TAG, "Current node index updated")

                val currentNodeIndex = _uiState.value.currentNodeIndex

                // sets current node image path to be shown
                _uiState.update {
                    it.copy(
                        currentNodePath = _uiState.value.route!!.sites[currentNodeIndex!!].img
                    )
                }
                Log.d(TAG, "Current node path updated")

                // sets first node image
                updateSiteImage()
            }
        }
    }


    /**
     * Returns the image associated with the URL
     */
    suspend fun updateSiteImage() {
        Log.d(TAG, "Current node image updated")
        // checks the current node
        if (_uiState.value.currentNodePath != null) {

            viewModelScope.launch (Dispatchers.IO) {
                _uiState.update {
                    it.copy(
                        currentNodeImage = highPrecisionRouteModel.getSiteImage(
                            "${uiState.value.currentNodePath}"
                        ) // TODO: ugly fix. Path is burned
                    )
                }
            }
        }
    }
}
