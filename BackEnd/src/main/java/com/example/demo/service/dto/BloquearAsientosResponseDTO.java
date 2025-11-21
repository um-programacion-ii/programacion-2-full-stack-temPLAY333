package com.example.demo.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * DTO de salida para respuesta de bloquear asientos.
 * Ejemplo JSON:
 * {
 *   "resultado": false,
 *   "descripcion": "No todos los asientos pueden ser bloqueados",
 *   "eventoId": 1,
 *   "asientos": [ { "estado": "Ocupado", "fila": 2, "columna": 1 }, ... ]
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BloquearAsientosResponseDTO implements Serializable {

    @NotNull
    private Boolean resultado;

    private String descripcion;

    @NotNull
    private Long eventoId;

    @NotNull
    @Size(min = 1)
    private List<AsientoBloqueoEstadoDTO> asientos;

    public Boolean getResultado() { return resultado; }
    public void setResultado(Boolean resultado) { this.resultado = resultado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public List<AsientoBloqueoEstadoDTO> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoBloqueoEstadoDTO> asientos) { this.asientos = asientos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BloquearAsientosResponseDTO)) return false;
        BloquearAsientosResponseDTO that = (BloquearAsientosResponseDTO) o;
        return Objects.equals(resultado, that.resultado) && Objects.equals(eventoId, that.eventoId) && Objects.equals(asientos, that.asientos);
    }

    @Override
    public int hashCode() { return Objects.hash(resultado, eventoId, asientos); }

    @Override
    public String toString() { return "BloquearAsientosResponseDTO{" + "resultado=" + resultado + ", eventoId=" + eventoId + '}'; }
}

