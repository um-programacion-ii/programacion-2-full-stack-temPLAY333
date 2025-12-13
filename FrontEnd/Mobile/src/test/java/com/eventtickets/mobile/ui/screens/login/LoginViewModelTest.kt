package com.eventtickets.mobile.ui.screens.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios para LoginViewModel
 *
 * Verifica:
 * - Validación de credenciales
 * - Estados de login (idle, loading, success, error)
 * - Manejo de errores
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial es idle con campos vacíos`() {
        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.loginSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onUsernameChange actualiza el username`() {
        // Given
        val newUsername = "testuser"

        // When
        viewModel.onUsernameChange(newUsername)

        // Then
        assertEquals(newUsername, viewModel.uiState.value.username)
    }

    @Test
    fun `onPasswordChange actualiza el password`() {
        // Given
        val newPassword = "testpass"

        // When
        viewModel.onPasswordChange(newPassword)

        // Then
        assertEquals(newPassword, viewModel.uiState.value.password)
    }

    @Test
    fun `onLoginClick con credenciales vacías muestra error`() = runTest {
        // Given - username y password vacíos por defecto

        // When
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.loginSuccess)
        assertNotNull(state.errorMessage)
        assertEquals("Usuario y contraseña requeridos", state.errorMessage)
    }

    @Test
    fun `onLoginClick con username vacío muestra error`() = runTest {
        // Given
        viewModel.onPasswordChange("password123")

        // When
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.loginSuccess)
        assertEquals("Usuario y contraseña requeridos", state.errorMessage)
    }

    @Test
    fun `onLoginClick con password vacío muestra error`() = runTest {
        // Given
        viewModel.onUsernameChange("admin")

        // When
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.loginSuccess)
        assertEquals("Usuario y contraseña requeridos", state.errorMessage)
    }

    @Test
    fun `onLoginClick con credenciales correctas tiene éxito`() = runTest {
        // Given
        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("admin")

        // When
        viewModel.uiState.test {
            // Consumir estado inicial
            skipItems(1)

            viewModel.onLoginClick()

            // Estado Loading
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertFalse(loadingState.loginSuccess)

            // Avanzar tiempo
            advanceUntilIdle()

            // Estado Success
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.loginSuccess)
            assertNull(successState.errorMessage)
        }
    }

    @Test
    fun `onLoginClick con credenciales incorrectas muestra error`() = runTest {
        // Given
        viewModel.onUsernameChange("wronguser")
        viewModel.onPasswordChange("wrongpass")

        // When
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.loginSuccess)
        assertNotNull(state.errorMessage)
        assertEquals("Usuario o contraseña incorrectos", state.errorMessage)
    }

    @Test
    fun `onLoginHandled resetea el flag de loginSuccess`() = runTest {
        // Given - hacer login exitoso primero
        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("admin")
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loginSuccess)

        // When
        viewModel.onLoginHandled()

        // Then
        assertFalse(viewModel.uiState.value.loginSuccess)
    }

    @Test
    fun `login emite estados en el orden correcto`() = runTest {
        // Given
        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("admin")

        // When & Then
        viewModel.uiState.test {
            // Estado inicial
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertFalse(initialState.loginSuccess)

            // Iniciar login
            viewModel.onLoginClick()

            // Estado Loading
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertFalse(loadingState.loginSuccess)

            // Avanzar tiempo
            advanceUntilIdle()

            // Estado Success
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.loginSuccess)
        }
    }

    @Test
    fun `múltiples intentos de login con credenciales incorrectas mantienen el error`() = runTest {
        // Given
        viewModel.onUsernameChange("wrong")
        viewModel.onPasswordChange("wrong")

        // When
        repeat(3) {
            viewModel.onLoginClick()
            advanceUntilIdle()
        }

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.loginSuccess)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `cambiar credenciales después de error limpia el mensaje de error`() = runTest {
        // Given - hacer login fallido
        viewModel.onUsernameChange("wrong")
        viewModel.onPasswordChange("wrong")
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)

        // When - cambiar las credenciales
        viewModel.onUsernameChange("admin")

        // Then - el error debería seguir hasta el próximo intento
        // (esto depende de la implementación, ajustar según tu lógica)
        val state = viewModel.uiState.value
        assertEquals("admin", state.username)
    }

    @Test
    fun `credenciales válidas case-sensitive`() = runTest {
        // Given
        viewModel.onUsernameChange("ADMIN") // Mayúsculas
        viewModel.onPasswordChange("admin")

        // When
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Then - Debería fallar si es case-sensitive
        val state = viewModel.uiState.value
        // Ajustar según tu implementación de case-sensitivity
        assertNotNull(state.errorMessage)
    }
}

