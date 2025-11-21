package um.prog2.dto.consultaventas;

import um.prog2.dto.venta.AsientoVentaEstadoDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DTO de salida para el endpoint GET /api/endpoints/v1/listar-venta/{id}.
 * Incluye detalle de la venta y los asientos asociados (si existieron).
 * Ejemplos:
 * {
 *   "eventoId": 1,
 *   "ventaId": 1503,
 *   "fechaVenta": "2025-08-23T22:51:02.574851Z",
 *   "asientos": [],
 *   "resultado": false,
 *   "descripcion": "Venta rechazada...",
 *   "precioVenta": 1200.1
 * }
 * y
 * {
 *   "eventoId": 1,
 *   "ventaId": 1504,
 *   "fechaVenta": "2025-08-23T22:51:15.101553Z",
 *   "asientos": [ { "fila": 2, "columna": 1, "persona": "Fernando Villarreal", "estado": "Ocupado" }, ... ],
 *   "resultado": true,
 *   "descripcion": "Venta realizada con exito",
 *   "precioVenta": 1200.1
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VentaDTO implements Serializable {

    @NotNull
    private Long eventoId;

    @NotNull
    private Long ventaId;

    @NotNull
    private Instant fechaVenta;

    @NotNull
    @Size(min = 0)
    private List<AsientoVentaEstadoDTO> asientos; // puede ser lista vacía

    @NotNull
    private Boolean resultado;

    @NotNull
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
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof VentaDTO)) return false; VentaDTO that = (VentaDTO) o; return Objects.equals(ventaId, that.ventaId); }
    @Override
    public int hashCode() { return Objects.hash(ventaId); }
    @Override
    public String toString() { return "VentaDetalleDTO{" + "ventaId=" + ventaId + ", resultado=" + resultado + '}'; }
}
