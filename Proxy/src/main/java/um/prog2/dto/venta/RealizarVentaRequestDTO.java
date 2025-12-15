package um.prog2.dto.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DTO de solicitud para el endpoint POST /api/endpoints/v1/realizar-venta.
 * JSON ejemplo:
 * {
 *   "eventoId": 1,
 *   "fecha": "2025-08-17T20:00:00.000Z",
 *   "precioVenta": 1400.10,
 *   "asientos": [ { "fila": 2, "columna": 3, "persona": "Fernando Galvez" }, ... ]
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RealizarVentaRequestDTO implements Serializable {

    @NotNull
    private Long eventoId;

    @NotNull
    private Instant fecha;

    @NotNull
    private BigDecimal precioVenta;

    @NotNull
    @Size(min = 1)
    private List<AsientoVentaDTO> asientos;

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public List<AsientoVentaDTO> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoVentaDTO> asientos) { this.asientos = asientos; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof RealizarVentaRequestDTO)) return false; RealizarVentaRequestDTO that = (RealizarVentaRequestDTO) o; return Objects.equals(eventoId, that.eventoId) && Objects.equals(fecha, that.fecha) && Objects.equals(precioVenta, that.precioVenta) && Objects.equals(asientos, that.asientos); }
    @Override
    public int hashCode() { return Objects.hash(eventoId, fecha, precioVenta, asientos); }
    @Override
    public String toString() { return "RealizarVentaRequestDTO{" + "eventoId=" + eventoId + ", fecha=" + fecha + ", precioVenta=" + precioVenta + '}'; }
}
