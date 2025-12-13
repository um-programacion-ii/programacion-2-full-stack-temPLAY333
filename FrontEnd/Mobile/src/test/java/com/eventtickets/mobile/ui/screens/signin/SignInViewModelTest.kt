package com.eventtickets.mobile.ui.screens.signin

import com.eventtickets.mobile.data.network.dto.RegisterResponse
import com.eventtickets.mobile.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para SignInViewModel - funcionalidad de registro
 * Tests simplificados que funcionan con el código real
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private lateinit var viewModel: SignInViewModel
    private lateinit var mockAuthRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockAuthRepository = mockk(relaxed = true)
        viewModel = SignInViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `estado inicial es correcto`() {
        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.phone)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.signInSuccess)
    }

    @Test
    fun `onNameChange actualiza el nombre`() {
        // When
        viewModel.onNameChange("Juan Pérez")

        // Then
        assertEquals("Juan Pérez", viewModel.uiState.value.name)
    }

    @Test
    fun `onEmailChange actualiza el email`() {
        // When
        viewModel.onEmailChange("juan@ejemplo.com")

        // Then
        assertEquals("juan@ejemplo.com", viewModel.uiState.value.email)
    }

    @Test
    fun `onPasswordChange actualiza la contraseña`() {
        // When
        viewModel.onPasswordChange("password123")

        // Then
        assertEquals("password123", viewModel.uiState.value.password)
    }

    @Test
    fun `onConfirmPasswordChange actualiza confirmación`() {
        // When
        viewModel.onConfirmPasswordChange("password123")

        // Then
        assertEquals("password123", viewModel.uiState.value.confirmPassword)
    }

    @Test
    fun `onSignInClick con nombre vacío muestra error`() = runTest {
        // Given
        viewModel.onEmailChange("juan@ejemplo.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        // When
        viewModel.onSignInClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("El nombre es obligatorio", state.errorMessage)
        assertFalse(state.signInSuccess)
    }

    @Test
    fun `onSignInClick con email vacío muestra error`() = runTest {
        // Given
        viewModel.onNameChange("Juan Pérez")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        // When
        viewModel.onSignInClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("El email es obligatorio", state.errorMessage)
        assertFalse(state.signInSuccess)
    }

    @Test
    fun `onSignInClick con email inválido muestra error`() = runTest {
        // Given
        viewModel.onNameChange("Juan Pérez")
        viewModel.onEmailChange("emailinvalido")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        // When
        viewModel.onSignInClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Email inválido", state.errorMessage)
        assertFalse(state.signInSuccess)
    }

    @Test
    fun `onSignInClick con contraseña corta muestra error`() = runTest {
        // Given
        viewModel.onNameChange("Juan Pérez")
        viewModel.onEmailChange("juan@ejemplo.com")
        viewModel.onPasswordChange("12345")
        viewModel.onConfirmPasswordChange("12345")

        // When
        viewModel.onSignInClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("La contraseña debe tener al menos 6 caracteres", state.errorMessage)
        assertFalse(state.signInSuccess)
    }

    @Test
    fun `onSignInClick con contraseñas no coincidentes muestra error`() = runTest {
        // Given
        viewModel.onNameChange("Juan Pérez")
        viewModel.onEmailChange("juan@ejemplo.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password456")

        // When
        viewModel.onSignInClick()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Las contraseñas no coinciden", state.errorMessage)
        assertFalse(state.signInSuccess)
    }

    @Test
    fun `validación ocurre antes de llamar al repository`() = runTest {
        // Given
        viewModel.onNameChange("Juan")
        viewModel.onEmailChange("juan@ejemplo.com")
        viewModel.onPasswordChange("123") // Muy corta
        viewModel.onConfirmPasswordChange("123")

        coEvery {
            mockAuthRepository.register(any(), any(), any())
        } returns Result.success(RegisterResponse("OK", 1L, "juan"))

        // When
        viewModel.onSignInClick()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { mockAuthRepository.register(any(), any(), any()) }
        assertEquals("La contraseña debe tener al menos 6 caracteres", viewModel.uiState.value.errorMessage)
    }
}

