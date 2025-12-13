package com.eventtickets.mobile.data

import com.eventtickets.mobile.data.model.AsientoMapaDto
import com.eventtickets.mobile.data.model.Event
import com.eventtickets.mobile.data.model.EventoDetalle
import com.eventtickets.mobile.data.model.EventoTipo
import com.eventtickets.mobile.data.model.Integrante
import com.eventtickets.mobile.data.model.MapaAsientosDto
import com.eventtickets.mobile.data.model.Purchase
import com.eventtickets.mobile.data.model.PurchaseDetail
import com.eventtickets.mobile.data.model.Seat
import com.eventtickets.mobile.data.model.SeatState

object MockData {
    val sampleEventos = listOf(
        Event(
            id = 1L,
            titulo = "Concierto de Rock Sinfónico",
            resumen = "Una noche épica donde el rock clásico se encuentra con la majestuosidad de una orquesta sinfónica.",
            fecha = "2024-10-26T20:00:00Z",
            imagen = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            eventoTipo = EventoTipo(1, "Música")
        ),
        Event(
            id = 2L,
            titulo = "Final de Conferencia",
            resumen = "El partido decisivo que define al campeón de la conferencia. Un encuentro lleno de emoción y adrenalina.",
            fecha = "2024-11-15T18:00:00Z",
            imagen = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            eventoTipo = EventoTipo(2, "Deportes")
        ),
        Event(
            id = 3L,
            titulo = "Obra de Teatro Clásico",
            resumen = "Una reinterpretación moderna de un clásico de la literatura. Una experiencia teatral única e inolvidable.",
            fecha = "2024-12-05T21:00:00Z",
            imagen = "https://images.unsplash.com/photo-1507924538820-ede94a04019d?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            eventoTipo = EventoTipo(3, "Teatro")
        )
    )

    private val sampleEventoDetalle = EventoDetalle(
        id = 1L,
        titulo = "Concierto de Rock Sinfónico",
        resumen = "Una noche épica donde el rock clásico se encuentra con la majestuosidad de una orquesta sinfónica.",
        descripcion = "Una experiencia sonora inigualable que fusiona la potencia del rock con la elegancia de la música clásica. La banda tributo 'Eternal Echoes' interpretará los grandes himnos de Queen, Led Zeppelin y Pink Floyd, acompañados por la Orquesta Filarmónica de la Ciudad. Prepárate para una noche de nostalgia, virtuosismo y arreglos espectaculares que te dejarán sin aliento.",
        fecha = "2024-10-26T20:00:00Z",
        direccion = "Gran Teatro Nacional, Av. Principal 123, Buenos Aires",
        imagen = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
        filaAsientos = 15,
        columnAsientos = 20,
        eventoTipo = EventoTipo(1, "Música", "Conciertos y festivales de todos los géneros."),
        integrantes = listOf(
            Integrante(1, "Alex Turner", "Vocalista"),
            Integrante(2, "Miles Kane", "Guitarrista"),
            Integrante(3, "Matt Helders", "Baterista")
        )
    )

    val samplePurchases = listOf(
        Purchase(
            id = 101L,
            evento = sampleEventos[0],
            asientos = listOf(
                Seat(id = "5-7", fila = 5, columna = 7, estado = SeatState.SOLD, precio = 1250.0),
                Seat(id = "5-8", fila = 5, columna = 8, estado = SeatState.SOLD, precio = 1250.0)
            ),
            precioVenta = 2500.0,
            fechaVenta = "2024-11-15T14:30:00Z"
        ),
        Purchase(
            id = 102L,
            evento = sampleEventos[1],
            asientos = listOf(
                Seat(id = "10-12", fila = 10, columna = 12, estado = SeatState.SOLD, precio = 850.0),
                Seat(id = "10-13", fila = 10, columna = 13, estado = SeatState.SOLD, precio = 850.0),
                Seat(id = "10-14", fila = 10, columna = 14, estado = SeatState.SOLD, precio = 850.0)
            ),
            precioVenta = 2550.0,
            fechaVenta = "2024-10-20T10:15:00Z"
        ),
        Purchase(
            id = 103L,
            evento = sampleEventos[2],
            asientos = listOf(
                Seat(id = "3-5", fila = 3, columna = 5, estado = SeatState.SOLD, precio = 950.0)
            ),
            precioVenta = 950.0,
            fechaVenta = "2024-09-05T16:45:00Z"
        ),
        Purchase(
            id = 104L,
            evento = sampleEventos[0],
            asientos = listOf(
                Seat(id = "8-10", fila = 8, columna = 10, estado = SeatState.SOLD, precio = 1250.0),
                Seat(id = "8-11", fila = 8, columna = 11, estado = SeatState.SOLD, precio = 1250.0),
                Seat(id = "8-12", fila = 8, columna = 12, estado = SeatState.SOLD, precio = 1250.0),
                Seat(id = "8-13", fila = 8, columna = 13, estado = SeatState.SOLD, precio = 1250.0)
            ),
            precioVenta = 5000.0,
            fechaVenta = "2024-12-01T19:20:00Z"
        )
    )

    private val samplePurchaseDetail = PurchaseDetail(
        id = 101L,
        fechaVenta = "2024-07-28T14:30:00Z",
        precioVenta = 2500.0,
        asientos = listOf(
            Seat(id = "5-7", fila = 5, columna = 7, estado = SeatState.SOLD, precio = 1250.0),
            Seat(id = "5-8", fila = 5, columna = 8, estado = SeatState.SOLD, precio = 1250.0)
        ),
        evento = PurchaseDetail.Evento(
            id = 1L,
            titulo = "Concierto de Rock Sinfónico",
            fecha = "2024-10-26T20:00:00Z",
            direccion = "Gran Teatro Nacional, Av. Principal 123, Buenos Aires"
        )
    )

    private val sampleSeatMap = MapaAsientosDto(
        eventoId = 1L,
        totalFilas = 15,
        totalColumnas = 20,
        asientos = listOf(
            AsientoMapaDto(fila = 2, columna = 5, estado = "Vendido"),
            AsientoMapaDto(fila = 2, columna = 6, estado = "Vendido"),
            AsientoMapaDto(fila = 5, columna = 10, estado = "Bloqueado", expira = "2024-12-01T20:00:00Z"),
            AsientoMapaDto(fila = 5, columna = 11, estado = "Bloqueado", expira = "2024-12-01T20:00:00Z"),
            AsientoMapaDto(fila = 10, columna = 3, estado = "Vendido")
        )
    )

    fun getEventosResumidos(): List<Event> {
        return sampleEventos
    }

    fun getEventoDetalleById(id: Long): EventoDetalle? {
        // Para pruebas, devolver detalle basado en el evento resumido
        val eventoResumido = sampleEventos.find { it.id == id } ?: return null

        return EventoDetalle(
            id = eventoResumido.id,
            titulo = eventoResumido.titulo,
            resumen = eventoResumido.resumen,
            descripcion = "Descripción completa del evento ${eventoResumido.titulo}. " +
                    "Una experiencia única que no te puedes perder. " +
                    "Artistas de talla mundial se presentarán en este evento especial.",
            fecha = eventoResumido.fecha,
            direccion = when (id) {
                1L -> "Gran Teatro Nacional, Av. Corrientes 1234, Buenos Aires"
                2L -> "Estadio Monumental, Av. Figueroa Alcorta 7597, Buenos Aires"
                3L -> "Teatro Colón, Cerrito 628, Buenos Aires"
                else -> "Ubicación por definir"
            },
            imagen = eventoResumido.imagen,
            filaAsientos = 15,
            columnAsientos = 20,
            eventoTipo = EventoTipo(
                id = eventoResumido.eventoTipo.id,
                nombre = eventoResumido.eventoTipo.nombre,
                descripcion = "Eventos de categoría ${eventoResumido.eventoTipo.nombre}"
            ),
            integrantes = listOf(
                Integrante(1, "Artista Principal", "Estrella"),
                Integrante(2, "Artista Secundario", "Invitado Especial"),
                Integrante(3, "Banda de Apoyo", "Músicos")
            )
        )
    }

    fun getPurchaseById(id: Long): Purchase? {
        return PurchaseManager.getPurchaseById(id)
    }

    fun getPurchaseDetailById(id: Long): PurchaseDetail? {
        // Intentar obtener la compra del PurchaseManager
        val purchase = PurchaseManager.getPurchaseById(id) ?: return null

        // Convertir Purchase a PurchaseDetail
        return PurchaseDetail(
            id = purchase.id,
            fechaVenta = purchase.fechaVenta,
            precioVenta = purchase.precioVenta,
            asientos = purchase.asientos,
            evento = PurchaseDetail.Evento(
                id = purchase.evento.id,
                titulo = purchase.evento.titulo,
                fecha = purchase.evento.fecha,
                direccion = when (purchase.evento.id) {
                    1L -> "Gran Teatro Nacional, Av. Principal 123, Buenos Aires"
                    2L -> "Estadio Olímpico, Av. Insurgentes Sur 789, Buenos Aires"
                    3L -> "Teatro de la Ciudad, Calle Reforma 456, Buenos Aires"
                    else -> "Ubicación por definir"
                }
            )
        )
    }

    fun getSeatMapForEvent(eventId: Long): MapaAsientosDto? {
        // Verificar que el evento exista
        val evento = sampleEventos.find { it.id == eventId } ?: return null

        // Devolver mapa con algunos asientos ocupados/bloqueados para demostración
        return MapaAsientosDto(
            eventoId = eventId,
            totalFilas = 15,
            totalColumnas = 20,
            asientos = listOf(
                AsientoMapaDto(fila = 2, columna = 5, estado = "Vendido"),
                AsientoMapaDto(fila = 2, columna = 6, estado = "Vendido"),
                AsientoMapaDto(fila = 5, columna = 10, estado = "Vendido"),
                AsientoMapaDto(fila = 5, columna = 11, estado = "Vendido"),
                AsientoMapaDto(fila = 10, columna = 3, estado = "Vendido"),
                AsientoMapaDto(fila = 1, columna = 1, estado = "Vendido"),
                AsientoMapaDto(fila = 1, columna = 2, estado = "Vendido")
            )
        )
    }

    fun getAllPurchases(): List<Purchase> {
        return PurchaseManager.getAllPurchases()
    }
}
