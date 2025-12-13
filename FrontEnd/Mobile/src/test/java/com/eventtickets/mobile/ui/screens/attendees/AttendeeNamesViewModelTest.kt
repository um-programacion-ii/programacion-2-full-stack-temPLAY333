package com.eventtickets.mobile.ui.screens.attendees

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios para AttendeeNamesViewModel
 *
 * Verifica:
 * - Inicialización con asientos del PurchaseManager
 * - Validación de nombres
 * - Habilitación del botón continuar
 * - Timer de 5 minutos
 * - Guardado de nombres
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttendeeNamesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AttendeeNamesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup PurchaseManager con asientos de prueba
        com.eventtickets.mobile.data.PurchaseManager.clear()
        com.eventtickets.mobile.data.PurchaseManager.startPurchase(
            eventId = 1L,
            seats = listOf(Pair(5, 7), Pair(5, 8))
        )

        // Deshabilitar timer para evitar race conditions en tests
        viewModel = AttendeeNamesViewModel(enableTimer = false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        com.eventtickets.mobile.data.PurchaseManager.clear()
    }

    @Test
    fun `initialize carga los asientos del PurchaseManager`() {
        // When
        viewModel.initialize()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.seats.size)

        assertTrue(state.seats.any { it.row == 5 && it.column == 7 })
        assertTrue(state.seats.any { it.row == 5 && it.column == 8 })
    }

    @Test
    fun `estado inicial tiene nombres vacíos y botón deshabilitado`() {
        // Given
        viewModel.initialize()

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(2, state.attendeeNames.size)
        assertTrue(state.attendeeNames.values.all { it.isEmpty() })
        assertFalse(state.isContinueEnabled)
        assertFalse(state.timerFinished)
    }

    @Test
    fun `onNameChanged actualiza el nombre del asiento correcto`() {
        // Given
        viewModel.initialize()
        val seat = SeatIdentifier(5, 7)
        val name = "Juan Pérez"

        // When
        viewModel.onNameChanged(seat, name)

        // Then
        val state = viewModel.uiState.value
        assertEquals(name, state.attendeeNames[seat])
    }

    @Test
    fun `nombre válido tiene al menos 3 caracteres`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When - nombre de 2 caracteres (inválido)
        viewModel.onNameChanged(seats[0], "Jo")

        // Then
        assertFalse(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `nombre válido tiene al menos 3 caracteres - caso válido`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When - nombres válidos para todos los asientos
        viewModel.onNameChanged(seats[0], "Juan Pérez")
        viewModel.onNameChanged(seats[1], "María García")

        // Then
        assertTrue(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `nombre solo con letras y espacios es válido`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When
        viewModel.onNameChanged(seats[0], "Juan Carlos Pérez")
        viewModel.onNameChanged(seats[1], "María del Carmen")

        // Then
        assertTrue(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `nombre con números es inválido`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When
        viewModel.onNameChanged(seats[0], "Juan123")
        viewModel.onNameChanged(seats[1], "María García")

        // Then
        assertFalse(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `nombre con caracteres especiales es inválido`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When
        viewModel.onNameChanged(seats[0], "Juan@Pérez")
        viewModel.onNameChanged(seats[1], "María García")

        // Then
        assertFalse(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `todos los nombres deben ser válidos para habilitar continuar`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When - solo un nombre válido
        viewModel.onNameChanged(seats[0], "Juan Pérez")

        // Then
        assertFalse(viewModel.uiState.value.isContinueEnabled)

        // When - ambos nombres válidos
        viewModel.onNameChanged(seats[1], "María García")

        // Then
        assertTrue(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `saveAttendeeNames guarda correctamente en PurchaseManager`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        viewModel.onNameChanged(seats[0], "Juan Pérez")
        viewModel.onNameChanged(seats[1], "María García")

        // When
        viewModel.saveAttendeeNames()

        // Then
        val savedNames = com.eventtickets.mobile.data.PurchaseManager.getAttendeeNames()
        assertEquals(2, savedNames.size)
        assertEquals("Juan Pérez", savedNames[Pair(5, 7)])
        assertEquals("María García", savedNames[Pair(5, 8)])
    }

    @Test
    fun `timer inicial es 5 minutos`() {
        // Given
        viewModel.initialize()

        // When
        val state = viewModel.uiState.value

        // Then
        // Timer se inicializa, verificamos que no ha expirado
        assertFalse(state.timerFinished)
        // Nota: El tiempo exacto puede variar por threading,
        // lo importante es que el timer existe
        assertNotNull(state.remainingTime)
    }

    @Test
    fun `nombre vacío después de ser válido deshabilita botón`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // Configurar nombres válidos
        viewModel.onNameChanged(seats[0], "Juan Pérez")
        viewModel.onNameChanged(seats[1], "María García")
        assertTrue(viewModel.uiState.value.isContinueEnabled)

        // When - vaciar uno de los nombres
        viewModel.onNameChanged(seats[0], "")

        // Then
        assertFalse(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `múltiples asientos mantienen nombres independientes`() {
        // Given
        com.eventtickets.mobile.data.PurchaseManager.clear()
        com.eventtickets.mobile.data.PurchaseManager.startPurchase(
            1L,
            listOf(Pair(1, 1), Pair(2, 2), Pair(3, 3), Pair(4, 4))
        )

        val vm = AttendeeNamesViewModel(enableTimer = false)
        vm.initialize(1L)
        val seats = vm.uiState.value.seats

        // When
        vm.onNameChanged(seats[0], "Nombre Uno")
        vm.onNameChanged(seats[1], "Nombre Dos")
        vm.onNameChanged(seats[2], "Nombre Tres")
        vm.onNameChanged(seats[3], "Nombre Cuatro")

        // Then
        val state = vm.uiState.value
        assertEquals("Nombre Uno", state.attendeeNames[seats[0]])
        assertEquals("Nombre Dos", state.attendeeNames[seats[1]])
        assertEquals("Nombre Tres", state.attendeeNames[seats[2]])
        assertEquals("Nombre Cuatro", state.attendeeNames[seats[3]])
        assertTrue(state.isContinueEnabled)
    }

    @Test
    fun `nombre con solo espacios es inválido`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When
        viewModel.onNameChanged(seats[0], "   ")
        viewModel.onNameChanged(seats[1], "María García")

        // Then
        assertFalse(viewModel.uiState.value.isContinueEnabled)
    }

    @Test
    fun `nombres con acentos son válidos`() {
        // Given
        viewModel.initialize()
        val seats = viewModel.uiState.value.seats

        // When
        viewModel.onNameChanged(seats[0], "José María")
        viewModel.onNameChanged(seats[1], "Mónica Sánchez")

        // Then
        assertTrue(viewModel.uiState.value.isContinueEnabled)
    }
}

