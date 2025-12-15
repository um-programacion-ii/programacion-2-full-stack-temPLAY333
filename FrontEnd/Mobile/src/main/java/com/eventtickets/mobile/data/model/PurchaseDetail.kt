package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseDetail(
    val id: Long,
    val fechaVenta: String,
    val precioVenta: Double,
    val asientos: List<Seat>,
    val evento: Evento
) {
    @Serializable
    data class Evento(
        val id: Long,
        val titulo: String,
        val fecha: String,
        val direccion: String
    )
}
