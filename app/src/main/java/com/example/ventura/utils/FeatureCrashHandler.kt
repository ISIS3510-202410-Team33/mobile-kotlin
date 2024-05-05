package com.example.ventura.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys

class FeatureCrashHandler(screenName: String) {
    private val screenName = screenName

    public fun logCrash(featureName: String, e: Exception) {
        Log.d("crashlytics", "Crash logged")
        FirebaseCrashlytics.getInstance().setCustomKeys {
            key("screen", screenName)
            key("feature", featureName)
        }
        FirebaseCrashlytics.getInstance().log("Test Crash Class")
        FirebaseCrashlytics.getInstance().recordException(e)
        FirebaseCrashlytics.getInstance().sendUnsentReports()
    }
}