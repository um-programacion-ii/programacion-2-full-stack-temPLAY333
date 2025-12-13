package com.eventtickets.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

// ==================== AUTENTICACIÓN ====================

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("id_token")
    val idToken: String
)

// ==================== EVENTOS ====================

data class EventoResumenDTO(
    val id: Long,
    val titulo: String,
    val resumen: String,
    val fecha: String, // ISO-8601
    val imagen: String,
    @SerializedName("evento_tipo")
    val eventoTipo: EventoTipoDTO
)

data class EventoDetalleDTO(
    val id: Long,
    val titulo: String,
    val resumen: String,
    val descripcion: String,
    val fecha: String,
    val direccion: String,
    val imagen: String,
    @SerializedName("fila_asientos")
    val filaAsientos: Int,
    @SerializedName("column_asientos")
    val columnAsientos: Int,
    @SerializedName("evento_tipo")
    val eventoTipo: EventoTipoDTO,
    val integrantes: List<IntegranteDTO>
)

data class EventoTipoDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null
)

data class IntegranteDTO(
    val id: Long,
    val nombre: String,
    val rol: String
)

// ==================== ASIENTOS ====================

data class MapaAsientosDTO(
    @SerializedName("evento_id")
    val eventoId: Long,
    @SerializedName("total_filas")
    val totalFilas: Int,
    @SerializedName("total_columnas")
    val totalColumnas: Int,
    val asientos: List<AsientoMapaDTO>
)

data class AsientoMapaDTO(
    val fila: Int,
    val columna: Int,
    val estado: String, // "Disponible", "Vendido", "Bloqueado"
    val expira: String? = null // Solo para bloqueados
)

data class BloquearAsientosRequest(
    val asientos: List<AsientoSeleccionDTO>
)

data class AsientoSeleccionDTO(
    val fila: Int,
    val columna: Int
)

data class BloquearAsientosResponse(
    val mensaje: String,
    @SerializedName("bloqueados_hasta")
    val bloqueadosHasta: String, // ISO-8601
    val asientos: List<AsientoMapaDTO>
)

// ==================== VENTAS ====================

data class RealizarVentaRequest(
    val asientos: List<AsientoVentaDTO>
)

data class AsientoVentaDTO(
    val fila: Int,
    val columna: Int,
    @SerializedName("nombre_asistente")
    val nombreAsistente: String
)

data class RealizarVentaResponse(
    @SerializedName("venta_id")
    val ventaId: Long,
    val mensaje: String,
    @SerializedName("codigo_qr")
    val codigoQr: String? = null
)

data class VentaDTO(
    val id: Long,
    @SerializedName("fecha_venta")
    val fechaVenta: String,
    @SerializedName("precio_venta")
    val precioVenta: Double,
    val evento: EventoResumenDTO,
    val asientos: List<AsientoDTO>
)

data class VentaDetalleDTO(
    val id: Long,
    @SerializedName("fecha_venta")
    val fechaVenta: String,
    @SerializedName("precio_venta")
    val precioVenta: Double,
    val asientos: List<AsientoDTO>,
    val evento: EventoVentaDTO
)

data class EventoVentaDTO(
    val id: Long,
    val titulo: String,
    val fecha: String,
    val direccion: String
)

data class AsientoDTO(
    val id: String,
    val fila: Int,
    val columna: Int,
    val estado: String,
    val precio: Double,
    @SerializedName("nombre_asistente")
    val nombreAsistente: String? = null
)

