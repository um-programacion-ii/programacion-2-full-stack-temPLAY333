package com.eventtickets.mobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi
import kotlin.OptIn

@OptIn(InternalSerializationApi::class)
@Serializable
data class Event(
    val id: Long,
    val imagen: String,
    val titulo: String,
    val fecha: String,
    val categoria: String,
    val direccion: String,
    val descripcion: String,
    val integrantes: List<Integrante>,
    val filaAsientos: Int,
    val columnAsientos: Int
)
