package com.eventtickets.mobile.data.network

import com.eventtickets.mobile.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para comunicación con el backend
 * Base URL: http://localhost:8081 (o 10.0.2.2:8081 desde emulador Android)
 */
interface ApiService {

    // ==================== AUTENTICACIÓN ====================

    @POST("/api/authenticate")
    suspend fun authenticate(@Body credentials: LoginRequest): Response<LoginResponse>

    // ==================== EVENTOS ====================

    @GET("/api/eventos-consulta/resumidos")
    suspend fun getEventosResumidos(): Response<List<EventoResumenDTO>>

    @GET("/api/eventos-consulta/{id}")
    suspend fun getEventoDetalle(@Path("id") id: Long): Response<EventoDetalleDTO>

    // ==================== ASIENTOS ====================

    @GET("/api/asientos/evento/{eventoId}/mapa")
    suspend fun getMapaAsientos(@Path("eventoId") eventoId: Long): Response<MapaAsientosDTO>

    @POST("/api/asientos/evento/{eventoId}/bloquear")
    suspend fun bloquearAsientos(
        @Path("eventoId") eventoId: Long,
        @Body request: BloquearAsientosRequest
    ): Response<BloquearAsientosResponse>

    // ==================== VENTAS ====================

    @POST("/api/ventas/evento/{eventoId}/realizar")
    suspend fun realizarVenta(
        @Path("eventoId") eventoId: Long,
        @Body request: RealizarVentaRequest
    ): Response<RealizarVentaResponse>

    @GET("/api/ventas")
    suspend fun getVentas(): Response<List<VentaDTO>>

    @GET("/api/ventas/{id}")
    suspend fun getVentaDetalle(@Path("id") id: Long): Response<VentaDetalleDTO>
}

