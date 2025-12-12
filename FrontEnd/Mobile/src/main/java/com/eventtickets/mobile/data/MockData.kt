package com.eventtickets.mobile.data

import com.eventtickets.mobile.data.model.Event
import com.eventtickets.mobile.data.model.Integrante
import com.eventtickets.mobile.data.model.Purchase
import com.eventtickets.mobile.data.model.Seat
import com.eventtickets.mobile.data.model.SeatState

object MockData {
    val sampleEvents = listOf(
        Event(
            id = 1L,
            imagen = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            titulo = "Concierto de Rock Sinfónico",
            fecha = "2024-10-26T20:00:00Z",
            categoria = "Música",
            direccion = "Gran Teatro Nacional, CDMX",
            descripcion = "Una noche épica donde el rock clásico se encuentra con la majestuosidad de una orquesta sinfónica. Revive los grandes éxitos de leyendas del rock con arreglos espectaculares.",
            integrantes = listOf(
                Integrante("Alex Turner", "Vocalista"),
                Integrante("Miles Kane", "Guitarrista")
            ),
            filaAsientos = 15,
            columnAsientos = 20
        )
    )

    val samplePurchases = listOf(
        Purchase(
            id = 101L,
            evento = sampleEvents[0],
            asientos = listOf(
                Seat(id = "5-7", fila = 5, columna = 7, estado = SeatState.SOLD, precio = 1250.0),
                Seat(id = "5-8", fila = 5, columna = 8, estado = SeatState.SOLD, precio = 1250.0)
            ),
            precioVenta = 2500.0,
            fechaVenta = "2024-07-28T14:30:00Z"
        ),
        Purchase(
            id = 102L,
            evento = Event(
                id = 2L,
                imagen = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                titulo = "Concierto de Prueba Pasado",
                fecha = "2023-01-15T20:00:00Z",
                categoria = "Música",
                direccion = "Teatro Metropólitan, CDMX",
                descripcion = "Descripción de un evento pasado.",
                integrantes = emptyList(),
                filaAsientos = 10,
                columnAsientos = 10
            ),
            asientos = listOf(
                Seat(id = "2-3", fila = 2, columna = 3, estado = SeatState.SOLD, precio = 800.0)
            ),
            precioVenta = 800.0,
            fechaVenta = "2023-01-10T18:00:00Z"
        )
    )

    fun getEventById(id: Long): Event? {
        return sampleEvents.find { it.id == id }
    }

    fun getPurchaseById(id: Long): Purchase? {
        return samplePurchases.find { it.id == id }
    }
}
