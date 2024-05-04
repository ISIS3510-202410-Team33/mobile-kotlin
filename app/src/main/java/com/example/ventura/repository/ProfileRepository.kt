package com.example.ventura.repository

import com.example.ventura.data.models.Profile

interface ProfileRepository {
    val noneProfile: Profile
        get() = Profile(
            name = "Guest",
            email = "Unavailable",
            universityName = "Unavailable"
        )
}
