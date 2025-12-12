package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Integrante(
    val nombre: String,
    val rol: String
)
