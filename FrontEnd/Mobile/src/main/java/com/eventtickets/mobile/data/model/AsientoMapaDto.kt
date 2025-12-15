package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AsientoMapaDto(
    val fila: Int,
    val columna: Int,
    val estado: String,
    val expira: String? = null
)
