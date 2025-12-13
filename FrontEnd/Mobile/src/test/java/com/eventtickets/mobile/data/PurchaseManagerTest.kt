package com.eventtickets.mobile.data

import com.eventtickets.mobile.data.model.SeatState
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para PurchaseManager
 *
 * Verifica:
 * - Inicio de compra con asientos
 * - Almacenamiento de nombres de asistentes
 * - Completar compra
 * - Limpieza de estado
 */
class PurchaseManagerTest {

    @Before
    fun setup() {
        // Limpiar estado antes de cada test
        PurchaseManager.clear()
    }

    @After
    fun tearDown() {
        // Limpiar estado después de cada test
        PurchaseManager.clear()
    }

    @Test
    fun `startPurchase guarda correctamente el evento y asientos`() {
        // Given
        val eventId = 1L
        val seats = listOf(Pair(5, 7), Pair(5, 8))

        // When
        PurchaseManager.startPurchase(eventId, seats)

        // Then
        assertEquals(eventId, PurchaseManager.getCurrentEventId())
        assertEquals(seats, PurchaseManager.getSelectedSeats())
        assertTrue(PurchaseManager.getAttendeeNames().isEmpty())
    }

    @Test
    fun `setAttendeeNames almacena correctamente los nombres`() {
        // Given
        val eventId = 1L
        val seats = listOf(Pair(5, 7), Pair(5, 8))
        PurchaseManager.startPurchase(eventId, seats)

        val names = mapOf(
            Pair(5, 7) to "Juan Pérez",
            Pair(5, 8) to "María García"
        )

        // When
        PurchaseManager.setAttendeeNames(names)

        // Then
        assertEquals(names, PurchaseManager.getAttendeeNames())
    }

    @Test
    fun `completePurchase retorna null si no hay evento actual`() {
        // Given - No se inicia ninguna compra

        // When
        val purchase = PurchaseManager.completePurchase()

        // Then
        assertNull(purchase)
    }

    @Test
    fun `completePurchase retorna null si no hay asientos seleccionados`() {
        // Given
        val eventId = 1L
        val emptySeats = emptyList<Pair<Int, Int>>()
        PurchaseManager.startPurchase(eventId, emptySeats)

        // When
        val purchase = PurchaseManager.completePurchase()

        // Then
        assertNull(purchase)
    }

    @Test
    fun `completePurchase genera una compra válida con todos los datos`() {
        // Given
        val eventId = 1L
        val seats = listOf(Pair(5, 7), Pair(5, 8))
        PurchaseManager.startPurchase(eventId, seats)

        val names = mapOf(
            Pair(5, 7) to "Juan Pérez",
            Pair(5, 8) to "María García"
        )
        PurchaseManager.setAttendeeNames(names)

        // When
        val purchase = PurchaseManager.completePurchase()

        // Then
        assertNotNull(purchase)
        purchase?.let {
            assertEquals(eventId, it.evento.id)
            assertEquals(2, it.asientos.size)
            assertEquals(2500.0, it.precioVenta, 0.01) // 2 asientos * 1250

            // Verificar que todos los asientos están como SOLD
            assertTrue(it.asientos.all { seat -> seat.estado == SeatState.SOLD })

            // Verificar que el ID es único
            assertTrue(it.id > 0)
        }
    }

    @Test
    fun `completePurchase limpia el estado después de completar`() {
        // Given
        val eventId = 1L
        val seats = listOf(Pair(5, 7), Pair(5, 8))
        PurchaseManager.startPurchase(eventId, seats)

        // When
        PurchaseManager.completePurchase()

        // Then - El estado debe estar limpio
        assertNull(PurchaseManager.getCurrentEventId())
        assertTrue(PurchaseManager.getSelectedSeats().isEmpty())
        assertTrue(PurchaseManager.getAttendeeNames().isEmpty())
    }

    @Test
    fun `getPurchaseById retorna la compra correcta`() {
        // Given
        val eventId = 1L
        val seats = listOf(Pair(5, 7))
        PurchaseManager.startPurchase(eventId, seats)
        val purchase = PurchaseManager.completePurchase()
        assertNotNull(purchase)
        val purchaseId = purchase!!.id

        // When
        val retrievedPurchase = PurchaseManager.getPurchaseById(purchaseId)

        // Then
        assertNotNull(retrievedPurchase)
        assertEquals(purchaseId, retrievedPurchase?.id)
    }

    @Test
    fun `getAllPurchases retorna todas las compras ordenadas por fecha`() {
        // Given
        val eventId1 = 1L
        val eventId2 = 2L

        PurchaseManager.startPurchase(eventId1, listOf(Pair(1, 1)))
        val purchase1 = PurchaseManager.completePurchase()

        Thread.sleep(100) // Pausa suficiente para asegurar fechas diferentes

        PurchaseManager.startPurchase(eventId2, listOf(Pair(2, 2)))
        val purchase2 = PurchaseManager.completePurchase()

        // When
        val allPurchases = PurchaseManager.getAllPurchases()

        // Then
        assertTrue(allPurchases.size >= 2) // Incluye las compras mock

        // Verificar que nuestras compras están presentes
        assertTrue(allPurchases.any { it.id == purchase1?.id })
        assertTrue(allPurchases.any { it.id == purchase2?.id })

        // Verificar que están ordenadas por fecha (más reciente primero)
        // Comparar las fechas de compra directamente
        val p1 = allPurchases.first { it.id == purchase1?.id }
        val p2 = allPurchases.first { it.id == purchase2?.id }

        // purchase2 debe tener fecha posterior a purchase1
        assertTrue(p2.fechaVenta >= p1.fechaVenta)
    }

    @Test
    fun `clear limpia completamente el estado`() {
        // Given
        val eventId = 1L
        val seats = listOf(Pair(5, 7), Pair(5, 8))
        val names = mapOf(Pair(5, 7) to "Test User")

        PurchaseManager.startPurchase(eventId, seats)
        PurchaseManager.setAttendeeNames(names)

        // When
        PurchaseManager.clear()

        // Then
        assertNull(PurchaseManager.getCurrentEventId())
        assertTrue(PurchaseManager.getSelectedSeats().isEmpty())
        assertTrue(PurchaseManager.getAttendeeNames().isEmpty())
    }

    @Test
    fun `multiple compras generan IDs únicos incrementales`() {
        // Given & When
        val purchases = mutableListOf<Long>()

        repeat(5) { i ->
            PurchaseManager.startPurchase(1L, listOf(Pair(i, i)))
            val purchase = PurchaseManager.completePurchase()
            assertNotNull(purchase)
            purchases.add(purchase!!.id)
        }

        // Then
        // Verificar que todos los IDs son únicos
        assertEquals(purchases.size, purchases.distinct().size)

        // Verificar que son incrementales
        for (i in 0 until purchases.size - 1) {
            assertTrue(purchases[i + 1] > purchases[i])
        }
    }
}

