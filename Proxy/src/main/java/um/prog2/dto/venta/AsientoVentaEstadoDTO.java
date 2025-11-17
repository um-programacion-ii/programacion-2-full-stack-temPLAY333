package um.prog2.dto.venta;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de salida por asiento al realizar venta.
 * Ejemplo: { "fila":2, "columna":3, "persona":"Nombre", "estado":"Libre" }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AsientoVentaEstadoDTO implements Serializable {

    @NotNull
    private Integer fila;
    @NotNull
    private Integer columna;
    private String persona; // puede ser null si no se consignó
    @NotNull
    private String estado; // "Libre", "Ocupado", "Vendido", etc.

    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }
    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }
    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof AsientoVentaEstadoDTO)) return false; AsientoVentaEstadoDTO that = (AsientoVentaEstadoDTO) o; return Objects.equals(fila, that.fila) && Objects.equals(columna, that.columna) && Objects.equals(persona, that.persona) && Objects.equals(estado, that.estado); }
    @Override
    public int hashCode() { return Objects.hash(fila, columna, persona, estado); }
    @Override
    public String toString() { return "AsientoVentaEstadoDTO{" + "fila=" + fila + ", columna=" + columna + ", estado='" + estado + '\'' + '}'; }
}
