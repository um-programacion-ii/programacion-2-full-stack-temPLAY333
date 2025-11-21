package um.prog2.dto.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DTO de respuesta para el endpoint de realizar venta.
 * Debe reflejar el contrato dado por el usuario.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RealizarVentaResponseDTO implements Serializable {

    @NotNull
    private Long eventoId;

    private Long ventaId; // puede ser null si la venta fue rechazada

    @NotNull
    private Instant fechaVenta;

    @NotNull
    @Size(min = 1)
    private List<AsientoVentaEstadoDTO> asientos;

    @NotNull
    private Boolean resultado;

    private String descripcion;

    @NotNull
    private BigDecimal precioVenta;

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
    public Instant getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Instant fechaVenta) { this.fechaVenta = fechaVenta; }
    public List<AsientoVentaEstadoDTO> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoVentaEstadoDTO> asientos) { this.asientos = asientos; }
    public Boolean getResultado() { return resultado; }
    public void setResultado(Boolean resultado) { this.resultado = resultado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof RealizarVentaResponseDTO)) return false; RealizarVentaResponseDTO that = (RealizarVentaResponseDTO) o; return Objects.equals(eventoId, that.eventoId) && Objects.equals(ventaId, that.ventaId) && Objects.equals(fechaVenta, that.fechaVenta) && Objects.equals(asientos, that.asientos) && Objects.equals(resultado, that.resultado) && Objects.equals(precioVenta, that.precioVenta); }
    @Override
    public int hashCode() { return Objects.hash(eventoId, ventaId, fechaVenta, asientos, resultado, precioVenta); }
    @Override
    public String toString() { return "RealizarVentaResponseDTO{" + "eventoId=" + eventoId + ", ventaId=" + ventaId + ", resultado=" + resultado + '}'; }
}
