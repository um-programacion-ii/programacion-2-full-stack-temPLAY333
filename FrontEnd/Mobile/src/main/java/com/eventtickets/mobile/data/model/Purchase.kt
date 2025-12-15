package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    val id: Long,
    val evento: Event,
    val asientos: List<Seat>,
    val precioVenta: Double,
    val fechaVenta: String
)
