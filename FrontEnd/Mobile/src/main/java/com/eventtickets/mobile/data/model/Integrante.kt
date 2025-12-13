package com.eventtickets.mobile.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Integrante(
    val id: Long,
    val nombre: String,
    val rol: String
)
