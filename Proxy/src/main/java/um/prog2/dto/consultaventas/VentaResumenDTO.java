package um.prog2.dto.consultaventas;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO de salida para listado de ventas (exitosas y fallidas) del alumno.
 * Campos según contrato del endpoint /api/endpoints/v1/listar-ventas.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VentaResumenDTO implements Serializable {

    @NotNull
    private Long eventoId;

    @NotNull
    private Long ventaId;

    @NotNull
    private Instant fechaVenta;

    @NotNull
    private Boolean resultado;

    @NotNull
    private String descripcion;

    @NotNull
    private BigDecimal precioVenta;

    @NotNull
    private Integer cantidadAsientos;

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
    public Instant getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Instant fechaVenta) { this.fechaVenta = fechaVenta; }
    public Boolean getResultado() { return resultado; }
    public void setResultado(Boolean resultado) { this.resultado = resultado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public Integer getCantidadAsientos() { return cantidadAsientos; }
    public void setCantidadAsientos(Integer cantidadAsientos) { this.cantidadAsientos = cantidadAsientos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VentaResumenDTO)) return false;
        VentaResumenDTO that = (VentaResumenDTO) o;
        return Objects.equals(ventaId, that.ventaId);
    }

    @Override
    public int hashCode() { return Objects.hash(ventaId); }

    @Override
    public String toString() {
        return "VentaResumenDTO{" +
            "eventoId=" + eventoId +
            ", ventaId=" + ventaId +
            ", fechaVenta=" + fechaVenta +
            ", resultado=" + resultado +
            ", precioVenta=" + precioVenta +
            ", cantidadAsientos=" + cantidadAsientos +
            '}';
    }
}
