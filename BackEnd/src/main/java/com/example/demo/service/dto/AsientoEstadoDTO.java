package com.example.demo.service.dto;

import java.io.Serializable;

/**
 * DTO que representa el estado de un asiento (fila, columna, estado) tal como se lee desde Redis de la cátedra.
 */
public class AsientoEstadoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int fila;
    private int columna;
    private String estado;

    public AsientoEstadoDTO() {}

    public AsientoEstadoDTO(int fila, int columna, String estado) {
        this.fila = fila;
        this.columna = columna;
        this.estado = estado;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

