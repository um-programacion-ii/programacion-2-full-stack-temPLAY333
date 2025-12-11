package com.example.demo.service.dto;

import java.io.Serializable;

/**
 * DTO para representar un asiento a bloquear (solo fila y columna).
 */
public class AsientoSeleccionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer fila;
    private Integer columna;

    public AsientoSeleccionDTO() {}

    public AsientoSeleccionDTO(Integer fila, Integer columna) {
        this.fila = fila;
        this.columna = columna;
    }

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

    @Override
    public String toString() {
        return "AsientoSeleccionDTO{" +
            "fila=" + fila +
            ", columna=" + columna +
            '}';
    }
}

