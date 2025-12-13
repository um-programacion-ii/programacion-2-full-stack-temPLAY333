package com.eventtickets.mobile.data.repository

import com.eventtickets.mobile.data.network.RetrofitClient
import com.eventtickets.mobile.data.network.dto.RegisterRequest
import com.eventtickets.mobile.data.network.dto.RegisterResponse
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Tests para AuthRepository - método register()
 */
class AuthRepositoryRegisterTest {

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        authRepository = AuthRepository()

        // Mock del RetrofitClient
        mockkObject(RetrofitClient)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `register exitoso retorna RegisterResponse`() = runTest {
        // Given
        val username = "juan_perez"
        val email = "juan@ejemplo.com"
        val password = "password123"

        val expectedResponse = RegisterResponse(
            mensaje = "Usuario creado exitosamente",
            userId = 1L,
            username = username
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } returns Response.success(expectedResponse)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())

        // Verificar que se llamó con los parámetros correctos
        coVerify {
            mockApiService.register(
                match {
                    it.username == username &&
                    it.email == email &&
                    it.password == password
                }
            )
        }
    }

    @Test
    fun `register con username existente retorna error 400`() = runTest {
        // Given
        val username = "admin"
        val email = "admin@ejemplo.com"
        val password = "password123"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } returns Response.error(400, "".toResponseBody())

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Usuario o email ya existe", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register con email existente retorna error 400`() = runTest {
        // Given
        val username = "nuevo_usuario"
        val email = "admin@ejemplo.com" // Email ya existe
        val password = "password123"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } returns Response.error(400, "".toResponseBody())

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Usuario o email ya existe", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register con error de red retorna excepción`() = runTest {
        // Given
        val username = "juan_perez"
        val email = "juan@ejemplo.com"
        val password = "password123"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } throws Exception("Network error")

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error de conexión") == true)
    }

    @Test
    fun `register con error 500 del servidor retorna mensaje genérico`() = runTest {
        // Given
        val username = "juan_perez"
        val email = "juan@ejemplo.com"
        val password = "password123"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } returns Response.error(500, "".toResponseBody())

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error al crear cuenta: 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register con respuesta null retorna error`() = runTest {
        // Given
        val username = "juan_perez"
        val email = "juan@ejemplo.com"
        val password = "password123"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } returns Response.success(null)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `register valida formato de request`() = runTest {
        // Given
        val username = "juan_perez"
        val email = "juan@ejemplo.com"
        val password = "password123"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        val capturedRequest = slot<RegisterRequest>()

        coEvery {
            mockApiService.register(capture(capturedRequest))
        } returns Response.success(
            RegisterResponse("OK", 1L, username)
        )

        every { RetrofitClient.apiService } returns mockApiService

        // When
        authRepository.register(username, email, password)

        // Then
        assertEquals(username, capturedRequest.captured.username)
        assertEquals(email, capturedRequest.captured.email)
        assertEquals(password, capturedRequest.captured.password)
    }

    @Test
    fun `register con caracteres especiales en username funciona`() = runTest {
        // Given
        val username = "juan_pérez-123"
        val email = "juan@ejemplo.com"
        val password = "password123"

        val expectedResponse = RegisterResponse(
            mensaje = "Usuario creado exitosamente",
            userId = 1L,
            username = username
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.register(any())
        } returns Response.success(expectedResponse)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = authRepository.register(username, email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(username, result.getOrNull()?.username)
    }
}

