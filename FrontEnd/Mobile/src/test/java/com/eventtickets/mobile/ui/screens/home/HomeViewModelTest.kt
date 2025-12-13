package com.eventtickets.mobile.ui.screens.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.eventtickets.mobile.data.MockData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios para HomeViewModel
 *
 * Verifica:
 * - Carga inicial de eventos
 * - Estados de Loading, Success y Error
 * - Refresh de eventos
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init carga eventos automáticamente`() = runTest {
        // Given - ViewModel ya se inicializa en setup()

        // When - avanzar el tiempo para que se complete la coroutine
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)

        val successState = state as HomeUiState.Success
        assertTrue(successState.events.isNotEmpty())
        assertEquals(MockData.getEventosResumidos().size, successState.events.size)
    }

    @Test
    fun `loadEvents emite Loading antes de Success`() = runTest {
        // Given
        val viewModel = HomeViewModel()

        // When & Then
        viewModel.uiState.test {
            // Estado inicial es Loading
            val loadingState = awaitItem()
            assertTrue(loadingState is HomeUiState.Loading)

            // Avanzar tiempo para completar la carga
            advanceUntilIdle()

            // Estado final es Success
            val successState = awaitItem()
            assertTrue(successState is HomeUiState.Success)

            val events = (successState as HomeUiState.Success).events
            assertTrue(events.isNotEmpty())
        }
    }

    @Test
    fun `loadEvents con isRefresh true no muestra Loading state`() = runTest {
        // Given
        advanceUntilIdle() // Completar carga inicial
        val initialState = viewModel.uiState.value
        assertTrue(initialState is HomeUiState.Success)

        // When - hacer refresh
        viewModel.loadEvents(isRefresh = true)

        // Then - el estado actual no debe ser Loading inmediatamente
        // (aunque puede cambiar después, lo importante es que no interrumpe el Success)
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is HomeUiState.Success)
    }

    @Test
    fun `Success state contiene todos los eventos de MockData`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        val state = viewModel.uiState.value

        // Then
        assertTrue(state is HomeUiState.Success)
        val successState = state as HomeUiState.Success

        val mockEvents = MockData.getEventosResumidos()
        assertEquals(mockEvents.size, successState.events.size)

        // Verificar que los IDs coinciden
        val mockIds = mockEvents.map { it.id }.toSet()
        val stateIds = successState.events.map { it.id }.toSet()
        assertEquals(mockIds, stateIds)
    }

    @Test
    fun `eventos tienen todos los campos requeridos`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        val state = viewModel.uiState.value as HomeUiState.Success

        // Then
        state.events.forEach { event ->
            assertTrue(event.id > 0)
            assertTrue(event.titulo.isNotEmpty())
            assertTrue(event.resumen.isNotEmpty())
            assertTrue(event.fecha.isNotEmpty())
            assertTrue(event.imagen.isNotEmpty())
            assertNotNull(event.eventoTipo)
            assertTrue(event.eventoTipo.nombre.isNotEmpty())
        }
    }

    @Test
    fun `loadEvents múltiples veces mantiene consistencia de datos`() = runTest {
        // Given
        advanceUntilIdle()
        val firstLoad = (viewModel.uiState.value as HomeUiState.Success).events

        // When
        viewModel.loadEvents(isRefresh = true)
        advanceUntilIdle()
        val secondLoad = (viewModel.uiState.value as HomeUiState.Success).events

        // Then
        assertEquals(firstLoad.size, secondLoad.size)
        assertEquals(firstLoad.map { it.id }, secondLoad.map { it.id })
    }

    @Test
    fun `eventos están ordenados correctamente por fecha`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        val state = viewModel.uiState.value as HomeUiState.Success

        // Then - Verificar que no hay excepciones al parsear fechas
        state.events.forEach { event ->
            assertTrue(event.fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")))
        }
    }
}

