package com.example.demo.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO básico para tipo de evento en respuestas de detalle sin exponer campos adicionales.
 * JSON ejemplo:
 * { "nombre": "Conferencia", "descripcion": "Conferencia" }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoTipoBasicDTO implements Serializable {

    @NotNull
    @Size(max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof EventoTipoBasicDTO)) return false; EventoTipoBasicDTO that = (EventoTipoBasicDTO) o; return Objects.equals(nombre, that.nombre); }
    @Override
    public int hashCode() { return Objects.hash(nombre); }
    @Override
    public String toString() { return "EventoTipoBasicDTO{" + "nombre='" + nombre + '\'' + '}'; }
}

