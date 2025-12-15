package um.prog2.dto.venta;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de entrada para un asiento a vender.
 * { "fila": 2, "columna": 3, "persona": "Nombre Apellido" }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AsientoVentaDTO implements Serializable {

    @NotNull @Min(1)
    private Integer fila;

    @NotNull @Min(1)
    private Integer columna;

    @NotBlank
    private String persona;

    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }
    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }
    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof AsientoVentaDTO)) return false; AsientoVentaDTO that = (AsientoVentaDTO) o; return Objects.equals(fila, that.fila) && Objects.equals(columna, that.columna) && Objects.equals(persona, that.persona); }
    @Override
    public int hashCode() { return Objects.hash(fila, columna, persona); }
    @Override
    public String toString() { return "AsientoVentaDTO{" + "fila=" + fila + ", columna=" + columna + ", persona='" + persona + '\'' + '}'; }
}
