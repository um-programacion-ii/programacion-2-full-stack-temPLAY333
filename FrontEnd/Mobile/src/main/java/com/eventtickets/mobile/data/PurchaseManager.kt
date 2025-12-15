package com.eventtickets.mobile.data

import com.eventtickets.mobile.data.model.Seat
import com.eventtickets.mobile.data.model.Purchase
import com.eventtickets.mobile.data.model.SeatState

/**
 * Gestor temporal de compras para modo mock/demo
 * Mantiene el estado de la compra en progreso
 */
object PurchaseManager {

    // Estado de la compra en progreso
    private var currentEventId: Long? = null
    private var selectedSeats: List<Pair<Int, Int>> = emptyList() // (fila, columna)
    private var attendeeNames: Map<Pair<Int, Int>, String> = emptyMap() // (fila, columna) -> nombre

    // Lista mutable de compras realizadas (para demo)
    private val completedPurchases = mutableListOf<Purchase>()

    init {
        // Agregar compras mock iniciales
        completedPurchases.addAll(MockData.samplePurchases)
    }

    /**
     * Inicia una nueva compra
     */
    fun startPurchase(eventId: Long, seats: List<Pair<Int, Int>>) {
        currentEventId = eventId
        selectedSeats = seats
        attendeeNames = emptyMap()
    }

    /**
     * Guarda los nombres de los asistentes
     */
    fun setAttendeeNames(names: Map<Pair<Int, Int>, String>) {
        attendeeNames = names
    }

    /**
     * Obtiene los asientos seleccionados
     */
    fun getSelectedSeats(): List<Pair<Int, Int>> = selectedSeats

    /**
     * Obtiene el ID del evento actual
     */
    fun getCurrentEventId(): Long? = currentEventId

    /**
     * Obtiene los nombres de asistentes
     */
    fun getAttendeeNames(): Map<Pair<Int, Int>, String> = attendeeNames

    /**
     * Completa la compra y genera un Purchase
     */
    fun completePurchase(): Purchase? {
        val eventId = currentEventId ?: return null
        if (selectedSeats.isEmpty()) return null

        val evento = MockData.getEventosResumidos().find { it.id == eventId } ?: return null

        // Precio por asiento (mock)
        val precioBase = 1250.0

        // Crear lista de asientos
        val asientos = selectedSeats.map { (fila, columna) ->
            Seat(
                id = "$fila-$columna",
                fila = fila,
                columna = columna,
                estado = SeatState.SOLD,
                precio = precioBase
            )
        }

        val precioTotal = asientos.sumOf { it.precio }

        // Generar ID único para la compra
        val purchaseId = (completedPurchases.maxOfOrNull { it.id } ?: 100L) + 1

        // Fecha actual en formato ISO
        val fechaActual = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())

        val purchase = Purchase(
            id = purchaseId,
            evento = evento,
            asientos = asientos,
            precioVenta = precioTotal,
            fechaVenta = fechaActual
        )

        // Agregar a la lista de compras
        completedPurchases.add(purchase)

        // Limpiar estado
        clear()

        return purchase
    }

    /**
     * Obtiene todas las compras realizadas (incluyendo las nuevas)
     */
    fun getAllPurchases(): List<Purchase> {
        return completedPurchases.sortedByDescending { it.fechaVenta }
    }

    /**
     * Obtiene una compra por ID
     */
    fun getPurchaseById(id: Long): Purchase? {
        return completedPurchases.find { it.id == id }
    }

    /**
     * Limpia el estado de la compra actual
     */
    fun clear() {
        currentEventId = null
        selectedSeats = emptyList()
        attendeeNames = emptyMap()
    }
}

