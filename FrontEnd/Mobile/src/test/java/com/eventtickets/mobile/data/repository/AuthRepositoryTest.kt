package com.eventtickets.mobile.data.repository

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.eventtickets.mobile.data.network.RetrofitClient
import com.eventtickets.mobile.data.network.dto.LoginRequest
import com.eventtickets.mobile.data.network.dto.LoginResponse
import retrofit2.Response

/**
 * Tests unitarios para AuthRepository
 *
 * Verifica:
 * - Login exitoso
 * - Login fallido
 * - Guardado de token
 * - Logout
 * - Verificación de estado logueado
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = AuthRepository()
        // Limpiar token antes de cada test
        RetrofitClient.setAuthToken(null)
    }

    @After
    fun tearDown() {
        RetrofitClient.setAuthToken(null)
    }

    @Test
    fun `login exitoso guarda el token`() = runTest {
        // Given
        val username = "admin"
        val password = "admin"
        val expectedToken = "fake-jwt-token-123"

        // Mock del ApiService
        mockkObject(RetrofitClient)
        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        every { RetrofitClient.apiService } returns mockApiService

        coEvery {
            mockApiService.authenticate(LoginRequest(username, password))
        } returns Response.success(LoginResponse(expectedToken))

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
        assertEquals(expectedToken, RetrofitClient.getAuthToken())

        unmockkAll()
    }

    @Test
    fun `login fallido no guarda token`() = runTest {
        // Given
        val username = "wrong"
        val password = "wrong"

        // Mock del ApiService
        mockkObject(RetrofitClient)
        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        every { RetrofitClient.apiService } returns mockApiService

        coEvery {
            mockApiService.authenticate(any())
        } returns Response.error(401, mockk(relaxed = true))

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isFailure)
        assertNull(RetrofitClient.getAuthToken())

        unmockkAll()
    }

    @Test
    fun `logout limpia el token`() {
        // Given
        RetrofitClient.setAuthToken("some-token")
        assertTrue(repository.isLoggedIn())

        // When
        repository.logout()

        // Then
        assertNull(RetrofitClient.getAuthToken())
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun `isLoggedIn retorna true cuando hay token`() {
        // Given
        RetrofitClient.setAuthToken("valid-token")

        // When
        val isLoggedIn = repository.isLoggedIn()

        // Then
        assertTrue(isLoggedIn)
    }

    @Test
    fun `isLoggedIn retorna false cuando no hay token`() {
        // Given
        RetrofitClient.setAuthToken(null)

        // When
        val isLoggedIn = repository.isLoggedIn()

        // Then
        assertFalse(isLoggedIn)
    }

    @Test
    fun `login con excepción retorna Result failure`() = runTest {
        // Given
        mockkObject(RetrofitClient)
        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        every { RetrofitClient.apiService } returns mockApiService

        coEvery {
            mockApiService.authenticate(any())
        } throws Exception("Network error")

        // When
        val result = repository.login("admin", "admin")

        // Then
        assertTrue(result.isFailure)
        assertNull(RetrofitClient.getAuthToken())

        unmockkAll()
    }

    @Test
    fun `múltiples logins sobrescriben el token anterior`() = runTest {
        // Given
        val firstToken = "token-1"
        val secondToken = "token-2"

        mockkObject(RetrofitClient)
        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        every { RetrofitClient.apiService } returns mockApiService

        // First login
        coEvery {
            mockApiService.authenticate(LoginRequest("user1", "pass1"))
        } returns Response.success(LoginResponse(firstToken))

        repository.login("user1", "pass1")
        assertEquals(firstToken, RetrofitClient.getAuthToken())

        // Second login
        coEvery {
            mockApiService.authenticate(LoginRequest("user2", "pass2"))
        } returns Response.success(LoginResponse(secondToken))

        // When
        repository.login("user2", "pass2")

        // Then
        assertEquals(secondToken, RetrofitClient.getAuthToken())
        assertNotEquals(firstToken, RetrofitClient.getAuthToken())

        unmockkAll()
    }
}

