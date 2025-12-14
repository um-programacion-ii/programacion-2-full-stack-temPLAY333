package com.eventtickets.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

// ==================== AUTENTICACIÓN ====================

data class RegisterRequest(
    val login: String,      // JHipster usa 'login' no 'username'
    val email: String,
    val password: String,
    val langKey: String = "es"  // Requerido por JHipster
)

// JHipster retorna 201 sin body
// No hay RegisterResponse en JHipster estándar

data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false
)

data class LoginResponse(
    @SerializedName("id_token")
    val idToken: String
)

// ==================== EVENTOS ====================

data class EventoResumenDTO(
    val id: Long,
    val titulo: String,
    val resumen: String? = null,
    val fecha: String, // ISO-8601
    // ⚠️ NO tiene campo "imagen" - solo disponible en EventoDetalleDTO
    @SerializedName("precio_entrada")
    val precioEntrada: Double? = null,
    @SerializedName("evento_tipo")
    val eventoTipo: EventoTipoDTO? = null
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
    val eventoId: Long? = null,
    val filas: Int,        // Backend usa "filas" no "total_filas"
    val columnas: Int,     // Backend usa "columnas" no "total_columnas"
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

// Backend NO espera nombres en realizar venta, solo fila/columna
// Los nombres no se envían según Backend-API.md
data class AsientoVentaDTO(
    val fila: Int,
    val columna: Int
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
    val resumen: String? = null,
    val fecha: String,
    val direccion: String,
    val imagen: String? = null,
    @SerializedName("precio_entrada")
    val precioEntrada: Double? = null
)

data class AsientoDTO(
    val id: String,
    val fila: Int,
    val columna: Int,
    val estado: String,
    val precio: Double,
    val persona: String? = null  // Backend usa 'persona' según Backend-API.md
)

