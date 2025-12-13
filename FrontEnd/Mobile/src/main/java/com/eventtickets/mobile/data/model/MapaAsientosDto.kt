package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MapaAsientosDto(
    val eventoId: Long,
    val totalFilas: Int,
    val totalColumnas: Int,
    val asientos: List<AsientoMapaDto>
)
