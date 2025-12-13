package com.eventtickets.mobile.data.repository

import com.eventtickets.mobile.data.model.Event
import com.eventtickets.mobile.data.model.EventoTipo
import com.eventtickets.mobile.data.network.RetrofitClient
import com.eventtickets.mobile.data.network.dto.EventoResumenDTO
import com.eventtickets.mobile.data.network.dto.EventoTipoDTO
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Tests para EventRepository - método buscarEventos()
 */
class EventRepositoryBusquedaTest {

    private lateinit var eventRepository: EventRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        eventRepository = EventRepository()

        // Mock del RetrofitClient
        mockkObject(RetrofitClient)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `buscarEventos con texto retorna eventos filtrados`() = runTest {
        // Given
        val texto = "rock"
        val mockEventos = listOf(
            EventoResumenDTO(
                id = 1L,
                titulo = "Concierto de Rock",
                resumen = "Una noche de rock",
                fecha = "2025-12-20T20:00:00Z",
                imagen = "https://example.com/rock.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", "Eventos musicales")
            )
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(texto, null)
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = texto)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Concierto de Rock", result.getOrNull()?.first()?.titulo)

        coVerify { mockApiService.buscarEventos(texto, null) }
    }

    @Test
    fun `buscarEventos con categoría retorna eventos de esa categoría`() = runTest {
        // Given
        val categoria = "Música"
        val mockEventos = listOf(
            EventoResumenDTO(
                id = 1L,
                titulo = "Festival de Jazz",
                resumen = "Los mejores músicos de jazz",
                fecha = "2025-12-25T19:30:00Z",
                imagen = "https://example.com/jazz.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            ),
            EventoResumenDTO(
                id = 2L,
                titulo = "Concierto de Rock",
                resumen = "Banda local de rock",
                fecha = "2025-12-20T20:00:00Z",
                imagen = "https://example.com/rock.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            )
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(null, categoria)
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(categoria = categoria)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()?.all { it.eventoTipo.nombre == "Música" } == true)

        coVerify { mockApiService.buscarEventos(null, categoria) }
    }

    @Test
    fun `buscarEventos con texto y categoría retorna eventos filtrados por ambos`() = runTest {
        // Given
        val texto = "rock"
        val categoria = "Música"
        val mockEventos = listOf(
            EventoResumenDTO(
                id = 1L,
                titulo = "Concierto de Rock",
                resumen = "Banda local de rock",
                fecha = "2025-12-20T20:00:00Z",
                imagen = "https://example.com/rock.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            )
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(texto, categoria)
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = texto, categoria = categoria)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Concierto de Rock", result.getOrNull()?.first()?.titulo)

        coVerify { mockApiService.buscarEventos(texto, categoria) }
    }

    @Test
    fun `buscarEventos sin parámetros retorna todos los eventos`() = runTest {
        // Given
        val mockEventos = listOf(
            EventoResumenDTO(
                id = 1L,
                titulo = "Evento 1",
                resumen = "Descripción 1",
                fecha = "2025-12-20T20:00:00Z",
                imagen = "https://example.com/1.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            ),
            EventoResumenDTO(
                id = 2L,
                titulo = "Evento 2",
                resumen = "Descripción 2",
                fecha = "2025-12-25T19:30:00Z",
                imagen = "https://example.com/2.jpg",
                eventoTipo = EventoTipoDTO(2L, "Deportes", null)
            ),
            EventoResumenDTO(
                id = 3L,
                titulo = "Evento 3",
                resumen = "Descripción 3",
                fecha = "2026-01-10T21:00:00Z",
                imagen = "https://example.com/3.jpg",
                eventoTipo = EventoTipoDTO(3L, "Teatro", null)
            )
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(null, null)
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)

        coVerify { mockApiService.buscarEventos(null, null) }
    }

    @Test
    fun `buscarEventos sin resultados retorna lista vacía`() = runTest {
        // Given
        val texto = "xyz123"
        val mockEventos = emptyList<EventoResumenDTO>()

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(texto, null)
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = texto)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `buscarEventos con error de red retorna failure`() = runTest {
        // Given
        val texto = "rock"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(any(), any())
        } throws Exception("Network error")

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = texto)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `buscarEventos con error 404 retorna failure con mensaje`() = runTest {
        // Given
        val texto = "rock"

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(texto, null)
        } returns Response.error(404, "".toResponseBody())

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = texto)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error al buscar eventos: 404", result.exceptionOrNull()?.message)
    }

    @Test
    fun `buscarEventos mapea correctamente DTOs a modelo de dominio`() = runTest {
        // Given
        val mockEventos = listOf(
            EventoResumenDTO(
                id = 1L,
                titulo = "Test Evento",
                resumen = "Test Resumen",
                fecha = "2025-12-20T20:00:00Z",
                imagen = "https://example.com/test.jpg",
                eventoTipo = EventoTipoDTO(1L, "Test Tipo", "Test Descripción")
            )
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(any(), any())
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = "test")

        // Then
        assertTrue(result.isSuccess)
        val evento = result.getOrNull()?.first()
        assertNotNull(evento)
        assertEquals(1L, evento?.id)
        assertEquals("Test Evento", evento?.titulo)
        assertEquals("Test Resumen", evento?.resumen)
        assertEquals("2025-12-20T20:00:00Z", evento?.fecha)
        assertEquals("https://example.com/test.jpg", evento?.imagen)
        assertEquals("Test Tipo", evento?.eventoTipo?.nombre)
        assertEquals("Test Descripción", evento?.eventoTipo?.descripcion)
    }

    @Test
    fun `buscarEventos con múltiples resultados retorna todos correctamente`() = runTest {
        // Given
        val mockEventos = listOf(
            EventoResumenDTO(
                id = 1L,
                titulo = "Rock Festival",
                resumen = "Gran festival de rock",
                fecha = "2025-12-20T20:00:00Z",
                imagen = "https://example.com/1.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            ),
            EventoResumenDTO(
                id = 2L,
                titulo = "Rock Concert",
                resumen = "Concierto de rock clásico",
                fecha = "2025-12-25T19:30:00Z",
                imagen = "https://example.com/2.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            ),
            EventoResumenDTO(
                id = 3L,
                titulo = "Rock Night",
                resumen = "Noche de rock en vivo",
                fecha = "2026-01-10T21:00:00Z",
                imagen = "https://example.com/3.jpg",
                eventoTipo = EventoTipoDTO(1L, "Música", null)
            )
        )

        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos("rock", null)
        } returns Response.success(mockEventos)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = "rock")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)

        val titulos = result.getOrNull()?.map { it.titulo }
        assertTrue(titulos?.contains("Rock Festival") == true)
        assertTrue(titulos?.contains("Rock Concert") == true)
        assertTrue(titulos?.contains("Rock Night") == true)
    }

    @Test
    fun `buscarEventos con respuesta null retorna failure`() = runTest {
        // Given
        val mockApiService = mockk<com.eventtickets.mobile.data.network.ApiService>()
        coEvery {
            mockApiService.buscarEventos(any(), any())
        } returns Response.success(null)

        every { RetrofitClient.apiService } returns mockApiService

        // When
        val result = eventRepository.buscarEventos(texto = "test")

        // Then
        assertTrue(result.isFailure)
    }
}

