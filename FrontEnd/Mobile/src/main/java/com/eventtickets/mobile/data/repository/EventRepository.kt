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

    suspend fun getEventosResumidos(): Result<List<Event>> {
        return try {
            val response = RetrofitClient.apiService.getEventosResumidos()

            if (response.isSuccessful && response.body() != null) {
                val eventos = response.body()!!.map { dto ->
                    Event(
                        id = dto.id,
                        titulo = dto.titulo,
                        resumen = dto.resumen,
                        fecha = dto.fecha,
                        imagen = dto.imagen,
                        eventoTipo = EventoTipo(
                            id = dto.eventoTipo.id,
                            nombre = dto.eventoTipo.nombre,
                            descripcion = dto.eventoTipo.descripcion
                        )
                    )
                }
                Result.success(eventos)
            } else {
                Result.failure(Exception("Error al cargar eventos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
}

