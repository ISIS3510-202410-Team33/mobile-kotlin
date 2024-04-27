package com.example.ventura

import android.app.Application
import com.example.ventura.model.databases.StepCountDatabase
import com.example.ventura.repository.StepCounterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class StepCounterApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { StepCountDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { StepCounterRepository(database.stepCountDao()) }
}