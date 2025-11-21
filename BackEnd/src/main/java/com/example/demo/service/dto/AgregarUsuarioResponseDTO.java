package com.example.demo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de respuesta para el endpoint POST /api/v1/agregar_usuario.
 *
 * JSON de salida ejemplo:
 * {
 *   "creado": true,
 *   "resultado": "Usuario creado",
 *   "token": "<jwt>"
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AgregarUsuarioResponseDTO implements Serializable {

    private boolean creado;

    @NotBlank
    private String resultado;

    @NotBlank
    private String token;

    public boolean isCreado() {
        return creado;
    }

    public void setCreado(boolean creado) {
        this.creado = creado;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgregarUsuarioResponseDTO)) return false;
        AgregarUsuarioResponseDTO that = (AgregarUsuarioResponseDTO) o;
        return creado == that.creado &&
            Objects.equals(resultado, that.resultado) &&
            Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creado, resultado, token);
    }

    @Override
    public String toString() {
        return "AgregarUsuarioResponseDTO{" +
            "creado=" + creado +
            ", resultado='" + resultado + '\'' +
            ", token='" + token + '\'' +
            '}';
    }
}

