package com.example.demo.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO básico de integrante para listarlo dentro del detalle del evento sin relaciones inversas.
 * JSON ejemplo: { "nombre": "María", "apellido": "Corvalán", "identificacion": "Dra." }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IntegranteBasicDTO implements Serializable {

    @NotNull
    @Size(max = 100)
    private String nombre;

    @NotNull
    @Size(max = 100)
    private String apellido;

    @NotNull
    @Size(max = 50)
    private String identificacion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof IntegranteBasicDTO)) return false; IntegranteBasicDTO that = (IntegranteBasicDTO) o; return Objects.equals(nombre, that.nombre) && Objects.equals(apellido, that.apellido) && Objects.equals(identificacion, that.identificacion); }
    @Override
    public int hashCode() { return Objects.hash(nombre, apellido, identificacion); }
    @Override
    public String toString() { return "IntegranteBasicDTO{" + "nombre='" + nombre + '\'' + ", apellido='" + apellido + '\'' + ", identificacion='" + identificacion + '\'' + '}'; }
}

