package com.example.demo.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de entrada para identificar un asiento por su fila y columna.
 * Ejemplo: { "fila": 2, "columna": 1 }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AsientoPosicionDTO implements Serializable {

    @NotNull
    @Min(1)
    private Integer fila;

    @NotNull
    @Min(1)
    private Integer columna;

    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }

    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsientoPosicionDTO)) return false;
        AsientoPosicionDTO that = (AsientoPosicionDTO) o;
        return Objects.equals(fila, that.fila) && Objects.equals(columna, that.columna);
    }

    @Override
    public int hashCode() { return Objects.hash(fila, columna); }

    @Override
    public String toString() { return "AsientoPosicionDTO{" + "fila=" + fila + ", columna=" + columna + '}'; }
}

