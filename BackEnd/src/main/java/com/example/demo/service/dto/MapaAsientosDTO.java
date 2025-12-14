package com.example.demo.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para el mapa de asientos de un evento.
 * Incluye ID del evento y lista de asientos con estado.
 */
public class MapaAsientosDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eventoId;
    private Integer filas;
    private Integer columnas;
    private List<AsientoRedisDTO> asientos; // Solo bloqueados/vendidos

    public MapaAsientosDTO() {}

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public Integer getFilas() {
        return filas;
    }

    public void setFilas(Integer filas) {
        this.filas = filas;
    }

    public Integer getColumnas() {
        return columnas;
    }

    public void setColumnas(Integer columnas) {
        this.columnas = columnas;
    }

    public List<AsientoRedisDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<AsientoRedisDTO> asientos) {
        this.asientos = asientos;
    }

    @Override
    public String toString() {
        return "MapaAsientosDTO{" +
            "eventoId=" + eventoId +
            ", filas=" + filas +
            ", columnas=" + columnas +
            ", asientos=" + (asientos != null ? asientos.size() : 0) + " asientos" +
            '}';
    }
}

