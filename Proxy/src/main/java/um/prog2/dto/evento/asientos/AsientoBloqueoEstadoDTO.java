package um.prog2.dto.evento.asientos;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de salida para el estado del bloqueo de un asiento.
 * Ejemplo: { "estado": "Ocupado", "fila": 2, "columna": 1 }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AsientoBloqueoEstadoDTO implements Serializable {

    @NotNull
    private String estado; // texto: "Bloqueado", "Ocupado", etc.

    @NotNull
    private Integer fila;

    @NotNull
    private Integer columna;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }
    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof AsientoBloqueoEstadoDTO)) return false; AsientoBloqueoEstadoDTO that = (AsientoBloqueoEstadoDTO) o; return Objects.equals(estado, that.estado) && Objects.equals(fila, that.fila) && Objects.equals(columna, that.columna); }
    @Override
    public int hashCode() { return Objects.hash(estado, fila, columna); }
    @Override
    public String toString() { return "AsientoBloqueoEstadoDTO{" + "estado='" + estado + '\'' + ", fila=" + fila + ", columna=" + columna + '}'; }
}
