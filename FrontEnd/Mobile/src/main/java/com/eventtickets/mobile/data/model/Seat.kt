package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Seat(
    val id: String, // e.g., "5-7"
    val fila: Int,
    val columna: Int,
    val estado: SeatState,
    val precio: Double
)
