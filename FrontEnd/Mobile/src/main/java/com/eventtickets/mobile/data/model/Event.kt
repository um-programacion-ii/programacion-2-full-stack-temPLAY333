package com.eventtickets.mobile.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Event(
    val id: Long,
    val titulo: String,
    val resumen: String,
    val fecha: String,
    val imagen: String,
    val eventoTipo: EventoTipo
)
