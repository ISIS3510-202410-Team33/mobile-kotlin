package com.example.ventura.data.models

import com.example.ventura.data.remote.SiteResponse

data class Site(
    val id: String = "",
    val name: String = "",
    val imgUrl: String = "",
    val type: String = "",
    val building: Building = Building()
)
