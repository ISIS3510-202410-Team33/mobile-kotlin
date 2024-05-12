package com.example.ventura.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

private const val TAG = "NetworkHandler"


/**
 * Service to monitor and manage network connections
 */
class NetworkHandler(
    application: Application
) {

    // interface for connectivity monitoring
    private var connectivityManager: ConnectivityManager = application
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    /**
     * Class used for the client Binder. Because we know this service always runs in the same
     * process as its clients, we don't need to deal with IPC
     * @see 'developer.android.com/develop/background-work/services/bound-services'
     */


    init {
        Log.d(TAG, "Created")
    }


    fun isInternetAvailable(): Boolean {
        Log.d(TAG, "Internet availability requested")
        // active network must be checked each time
        val network = connectivityManager.activeNetwork ?: return false
        // network capabilities must be checked each time
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}