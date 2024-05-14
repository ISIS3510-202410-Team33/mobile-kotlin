package com.example.ventura.utils

import android.app.Application

class DummyNetworkHandler(application: Application) : NetworkHandler(application) {

    override fun isInternetAvailable(): Boolean {
        return Math.random() > 0.5
    }
}