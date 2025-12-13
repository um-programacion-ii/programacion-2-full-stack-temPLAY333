package com.example.demo.service.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO para asiento con información de expiración (usado en Redis).
 */
public class AsientoRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer fila;
    private Integer columna;
    private String estado; // "Bloqueado", "Vendido", "Disponible"
    private Instant expira; // Solo para bloqueados

    public AsientoRedisDTO() {}

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getExpira() {
        return expira;
    }

    public void setExpira(Instant expira) {
        this.expira = expira;
    }

    @Override
    public String toString() {
        return "AsientoRedisDTO{" +
            "fila=" + fila +
            ", columna=" + columna +
            ", estado='" + estado + '\'' +
            ", expira=" + expira +
            '}';
    }
}

