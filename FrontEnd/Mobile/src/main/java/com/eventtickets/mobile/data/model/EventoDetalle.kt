package com.eventtickets.mobile.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class EventoDetalle(
    val id: Long,
    val titulo: String,
    val resumen: String,
    val descripcion: String,
    val fecha: String,
    val direccion: String,
    val imagen: String,
    val filaAsientos: Int,
    val columnAsientos: Int,
    val eventoTipo: EventoTipo,
    val integrantes: List<Integrante>
)
