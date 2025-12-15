package com.eventtickets.mobile.data.repository

import com.eventtickets.mobile.data.model.Event
import com.eventtickets.mobile.data.model.EventoDetalle
import com.eventtickets.mobile.data.model.EventoTipo
import com.eventtickets.mobile.data.model.Integrante
import com.eventtickets.mobile.data.network.RetrofitClient

/**
 * Repositorio para manejo de eventos
 */
class EventRepository {

    suspend fun buscarEventos(texto: String? = null, categoria: String? = null): Result<List<Event>> {
        return try {
            val response = RetrofitClient.apiService.buscarEventos(texto, categoria)

            if (response.isSuccessful && response.body() != null) {
                val eventos = response.body()!!.map { dto ->
                    val eventoTipo = dto.eventoTipo?.let {
                        EventoTipo(
                            id = it.id,
                            nombre = it.nombre,
                            descripcion = it.descripcion
                        )
                    } ?: EventoTipo(
                        id = 0,
                        nombre = "Sin categoría",
                        descripcion = ""
                    )

                    Event(
                        id = dto.id,
                        titulo = dto.titulo,
                        resumen = dto.resumen ?: "",
                        fecha = dto.fecha,
                        eventoTipo = eventoTipo
                    )
                }
                Result.success(eventos)
            } else {
                Result.failure(Exception("Error al buscar eventos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventosResumidos(): Result<List<Event>> {
        return try {
            println("[EventRepository] getEventosResumidos: iniciando llamada al ApiService")
            val response = RetrofitClient.apiService.getEventosResumidos()
            println("[EventRepository] getEventosResumidos: llamada finalizada con code=${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val eventos = response.body()!!.map { dto ->
                    val eventoTipo = dto.eventoTipo?.let {
                        EventoTipo(
                            id = it.id,
                            nombre = it.nombre,
                            descripcion = it.descripcion
                        )
                    } ?: EventoTipo(
                        id = 0,
                        nombre = "Sin categoría",
                        descripcion = ""
                    )

                    Event(
                        id = dto.id,
                        titulo = dto.titulo,
                        resumen = dto.resumen ?: "",
                        fecha = dto.fecha,
                        eventoTipo = eventoTipo
                    )
                }
                Result.success(eventos)
            } else {
                Result.failure(Exception("Error al cargar eventos: ${response.code()}"))
            }
        } catch (e: NullPointerException) {
            Result.failure(Exception("Error al procesar eventos: Algunos datos están incompletos"))
        } catch (e: Exception) {
            println("[EventRepository] getEventosResumidos: excepción -> ${e::class.simpleName}: ${e.message}")
            Result.failure(Exception("Error al cargar eventos: ${e.message}"))
        }
    }

    suspend fun getEventoDetalle(id: Long): Result<EventoDetalle> {
        return try {
            val response = RetrofitClient.apiService.getEventoDetalle(id)

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val evento = EventoDetalle(
                    id = dto.id,
                    titulo = dto.titulo,
                    resumen = dto.resumen,
                    descripcion = dto.descripcion,
                    fecha = dto.fecha,
                    direccion = dto.direccion,
                    imagen = dto.imagen,
                    filaAsientos = dto.filaAsientos,
                    columnAsientos = dto.columnAsientos,
                    eventoTipo = EventoTipo(
                        id = dto.eventoTipo.id,
                        nombre = dto.eventoTipo.nombre,
                        descripcion = dto.eventoTipo.descripcion
                    ),
                    integrantes = dto.integrantes.map { intDto ->
                        Integrante(
                            id = intDto.id,
                            nombre = intDto.nombre,
                            rol = intDto.rol
                        )
                    }
                )
                Result.success(evento)
            } else {
                Result.failure(Exception("Error al cargar detalle del evento: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMapaAsientos(eventoId: Long): Result<com.eventtickets.mobile.data.model.MapaAsientosDto> {
        return try {
            val response = RetrofitClient.apiService.getMapaAsientos(eventoId)

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val mapa = com.eventtickets.mobile.data.model.MapaAsientosDto(
                    eventoId = dto.eventoId ?: eventoId,
                    totalFilas = dto.filas,      // Backend usa "filas"
                    totalColumnas = dto.columnas, // Backend usa "columnas"
                    asientos = dto.asientos.map { asientoDto ->
                        com.eventtickets.mobile.data.model.AsientoMapaDto(
                            fila = asientoDto.fila,
                            columna = asientoDto.columna,
                            estado = asientoDto.estado
                        )
                    }
                )
                Result.success(mapa)
            } else {
                Result.failure(Exception("Error al cargar mapa de asientos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bloquearAsientos(
        eventoId: Long,
        asientos: List<Pair<Int, Int>>
    ): Result<com.eventtickets.mobile.data.network.dto.BloquearAsientosResponse> {
        return try {
            val request = com.eventtickets.mobile.data.network.dto.BloquearAsientosRequest(
                asientos = asientos.map { (fila, columna) ->
                    com.eventtickets.mobile.data.network.dto.AsientoSeleccionDTO(fila, columna)
                }
            )
            val response = RetrofitClient.apiService.bloquearAsientos(eventoId, request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al bloquear asientos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun realizarVenta(
        eventoId: Long,
        asientos: List<Triple<Int, Int, String>>  // Recibe nombres pero NO los envía al backend
    ): Result<com.eventtickets.mobile.data.network.dto.RealizarVentaResponse> {
        return try {
            // Backend NO espera nombres en realizar venta, solo fila/columna según Backend-API.md
            val asientosRequest = asientos.map { (fila, columna, _) ->
                com.eventtickets.mobile.data.network.dto.AsientoVentaDTO(fila, columna)
            }
            val response = RetrofitClient.apiService.realizarVenta(eventoId, asientosRequest)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al realizar venta: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVentas(): Result<List<com.eventtickets.mobile.data.model.Purchase>> {
        return try {
            val response = RetrofitClient.apiService.getVentas()

            if (response.isSuccessful && response.body() != null) {
                val ventas = response.body()!!.map { dto ->
                    // Crear un EventoTipo por defecto si viene null
                    val eventoTipo = dto.evento.eventoTipo?.let {
                        EventoTipo(
                            id = it.id,
                            nombre = it.nombre,
                            descripcion = it.descripcion
                        )
                    } ?: EventoTipo(
                        id = 0,
                        nombre = "Sin categoría",
                        descripcion = ""
                    )

                    com.eventtickets.mobile.data.model.Purchase(
                        id = dto.id,
                        evento = com.eventtickets.mobile.data.model.Event(
                            id = dto.evento.id,
                            titulo = dto.evento.titulo,
                            resumen = dto.evento.resumen ?: "",
                            fecha = dto.evento.fecha,
                            eventoTipo = eventoTipo
                        ),
                        asientos = dto.asientos.map { asientoDto ->
                            com.eventtickets.mobile.data.model.Seat(
                                id = asientoDto.id,
                                fila = asientoDto.fila,
                                columna = asientoDto.columna,
                                estado = try {
                                    com.eventtickets.mobile.data.model.SeatState.valueOf(asientoDto.estado.uppercase())
                                } catch (e: Exception) {
                                    com.eventtickets.mobile.data.model.SeatState.SOLD
                                },
                                precio = asientoDto.precio
                            )
                        },
                        precioVenta = dto.precioVenta,
                        fechaVenta = dto.fechaVenta
                    )
                }
                Result.success(ventas)
            } else {
                Result.failure(Exception("Error al cargar ventas: ${response.code()}"))
            }
        } catch (e: NullPointerException) {
            Result.failure(Exception("Error al procesar ventas: Algunos datos están incompletos. Verifica el formato del backend."))
        } catch (e: Exception) {
            Result.failure(Exception("Error al cargar ventas: ${e.message}"))
        }
    }

    suspend fun getVentaDetalle(id: Long): Result<com.eventtickets.mobile.data.model.PurchaseDetail> {
        return try {
            val response = RetrofitClient.apiService.getVentaDetalle(id)

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val detalle = com.eventtickets.mobile.data.model.PurchaseDetail(
                    id = dto.id,
                    fechaVenta = dto.fechaVenta,
                    precioVenta = dto.precioVenta,
                    asientos = dto.asientos.map { asientoDto ->
                        com.eventtickets.mobile.data.model.Seat(
                            id = asientoDto.id,
                            fila = asientoDto.fila,
                            columna = asientoDto.columna,
                            estado = com.eventtickets.mobile.data.model.SeatState.valueOf(asientoDto.estado.uppercase()),
                            precio = asientoDto.precio
                        )
                    },
                    evento = com.eventtickets.mobile.data.model.PurchaseDetail.Evento(
                        id = dto.evento.id,
                        titulo = dto.evento.titulo,
                        fecha = dto.evento.fecha,
                        direccion = dto.evento.direccion
                    )
                )
                Result.success(detalle)
            } else {
                Result.failure(Exception("Error al cargar detalle de venta: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

