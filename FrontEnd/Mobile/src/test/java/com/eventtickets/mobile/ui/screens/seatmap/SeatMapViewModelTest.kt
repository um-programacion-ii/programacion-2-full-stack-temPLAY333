package com.eventtickets.mobile.ui.screens.seatmap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios para SeatMapViewModel
 *
 * Verifica:
 * - Carga del mapa de asientos
 * - Selección/deselección de asientos
 * - Límite máximo de 4 asientos
 * - Confirmación de selección
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SeatMapViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SeatMapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SeatMapViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial es loading`() {
        // When
        val state = viewModel.uiState.value

        // Then
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertNull(state.seatMap)
        assertTrue(state.selectedSeats.isEmpty())
    }

    @Test
    fun `loadSeatMap carga correctamente el mapa de asientos`() = runTest {
        // Given
        val eventId = 1L

        // When
        viewModel.loadSeatMap(eventId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.seatMap)
        assertTrue(state.eventTitle.isNotEmpty())

        state.seatMap?.let { seatMap ->
            assertTrue(seatMap.totalFilas > 0)
            assertTrue(seatMap.totalColumnas > 0)
            assertEquals(eventId, seatMap.eventoId)
        }
    }

    @Test
    fun `loadSeatMap con evento inválido muestra error`() = runTest {
        // Given
        val invalidEventId = 999L

        // When
        viewModel.loadSeatMap(invalidEventId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertNull(state.seatMap)
    }

    @Test
    fun `toggleSeatSelection selecciona asiento disponible`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        val row = 3
        val col = 5

        // When
        viewModel.toggleSeatSelection(row, col)

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.selectedSeats.size)

        val selectedSeat = state.selectedSeats.first()
        assertEquals(row, selectedSeat.fila)
        assertEquals(col, selectedSeat.columna)
        assertEquals("Seleccionado", selectedSeat.estado)
    }

    @Test
    fun `toggleSeatSelection deselecciona asiento ya seleccionado`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        val row = 3
        val col = 5

        // Seleccionar primero
        viewModel.toggleSeatSelection(row, col)
        assertEquals(1, viewModel.uiState.value.selectedSeats.size)

        // When - Seleccionar nuevamente (toggle)
        viewModel.toggleSeatSelection(row, col)

        // Then
        val state = viewModel.uiState.value
        assertEquals(0, state.selectedSeats.size)
    }

    @Test
    fun `no permite seleccionar más de 4 asientos`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        // When - Intentar seleccionar 5 asientos disponibles (no los vendidos 1,1 y 1,2)
        viewModel.toggleSeatSelection(3, 3)
        viewModel.toggleSeatSelection(3, 4)
        viewModel.toggleSeatSelection(3, 5)
        viewModel.toggleSeatSelection(3, 6)

        // Verificar que tenemos 4
        assertEquals(4, viewModel.uiState.value.selectedSeats.size)

        // Intentar seleccionar el 5to (debería ser ignorado)
        viewModel.toggleSeatSelection(3, 7)

        // Then - sigue siendo 4
        val state = viewModel.uiState.value
        assertEquals(4, state.selectedSeats.size)
    }

    @Test
    fun `no permite seleccionar asiento vendido`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        // Asiento vendido según MockData (2, 5)
        val soldRow = 2
        val soldCol = 5

        // When
        viewModel.toggleSeatSelection(soldRow, soldCol)

        // Then
        val state = viewModel.uiState.value
        assertEquals(0, state.selectedSeats.size) // No debe seleccionarse
    }

    @Test
    fun `no permite seleccionar asiento bloqueado`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        // Asiento bloqueado según MockData (5, 10)
        val blockedRow = 5
        val blockedCol = 10

        // When
        viewModel.toggleSeatSelection(blockedRow, blockedCol)

        // Then
        val state = viewModel.uiState.value
        assertEquals(0, state.selectedSeats.size) // No debe seleccionarse
    }

    @Test
    fun `puede seleccionar múltiples asientos disponibles`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        // When
        viewModel.toggleSeatSelection(3, 3)
        viewModel.toggleSeatSelection(3, 4)
        viewModel.toggleSeatSelection(4, 3)

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.selectedSeats.size)

        // Verificar que todos están seleccionados
        assertTrue(state.selectedSeats.any { it.fila == 3 && it.columna == 3 })
        assertTrue(state.selectedSeats.any { it.fila == 3 && it.columna == 4 })
        assertTrue(state.selectedSeats.any { it.fila == 4 && it.columna == 3 })
    }

    @Test
    fun `confirmSelection guarda los asientos en PurchaseManager`() = runTest {
        // Given
        val eventId = 1L
        viewModel.loadSeatMap(eventId)
        advanceUntilIdle()

        viewModel.toggleSeatSelection(3, 3)
        viewModel.toggleSeatSelection(3, 4)

        // When
        viewModel.confirmSelection(eventId)

        // Then
        val purchaseManager = com.eventtickets.mobile.data.PurchaseManager
        assertEquals(eventId, purchaseManager.getCurrentEventId())
        assertEquals(2, purchaseManager.getSelectedSeats().size)

        val seats = purchaseManager.getSelectedSeats()
        assertTrue(seats.contains(Pair(3, 3)))
        assertTrue(seats.contains(Pair(3, 4)))
    }

    @Test
    fun `selectedSeats persiste entre cambios de estado`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        // When - seleccionar asientos disponibles uno por uno
        viewModel.toggleSeatSelection(3, 3)
        var currentSize = viewModel.uiState.value.selectedSeats.size
        assertEquals(1, currentSize)

        viewModel.toggleSeatSelection(3, 4)
        currentSize = viewModel.uiState.value.selectedSeats.size
        assertEquals(2, currentSize)

        // When - Agregar más asientos
        viewModel.toggleSeatSelection(3, 5)

        // Then - debe tener 3
        val finalSelection = viewModel.uiState.value.selectedSeats.size
        assertEquals(3, finalSelection)
    }

    @Test
    fun `puede deseleccionar asientos en cualquier orden`() = runTest {
        // Given
        viewModel.loadSeatMap(1L)
        advanceUntilIdle()

        // Seleccionar asientos disponibles
        viewModel.toggleSeatSelection(3, 3)
        viewModel.toggleSeatSelection(3, 4)
        viewModel.toggleSeatSelection(3, 5)
        assertEquals(3, viewModel.uiState.value.selectedSeats.size)

        // When - Deseleccionar el del medio
        viewModel.toggleSeatSelection(3, 4)

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.selectedSeats.size)
        assertTrue(state.selectedSeats.any { it.fila == 3 && it.columna == 3 })
        assertTrue(state.selectedSeats.any { it.fila == 3 && it.columna == 5 })
        assertFalse(state.selectedSeats.any { it.fila == 3 && it.columna == 4 })
    }
}

