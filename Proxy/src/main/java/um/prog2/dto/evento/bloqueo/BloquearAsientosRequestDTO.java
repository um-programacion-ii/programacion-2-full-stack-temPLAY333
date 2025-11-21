package um.prog2.dto.evento.bloqueo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * DTO de solicitud para bloquear asientos.
 * JSON ejemplo:
 * {
 *   "eventoId": 1,
 *   "asientos": [ { "fila": 2, "columna": 1 }, { "fila": 2, "columna": 2 } ]
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BloquearAsientosRequestDTO implements Serializable {

    @NotNull
    private Long eventoId;

    @NotNull
    @Size(min = 1)
    private List<AsientoPosicionDTO> asientos;

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public List<AsientoPosicionDTO> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoPosicionDTO> asientos) { this.asientos = asientos; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof BloquearAsientosRequestDTO)) return false; BloquearAsientosRequestDTO that = (BloquearAsientosRequestDTO) o; return Objects.equals(eventoId, that.eventoId) && Objects.equals(asientos, that.asientos); }
    @Override
    public int hashCode() { return Objects.hash(eventoId, asientos); }
    @Override
    public String toString() { return "BloquearAsientosRequestDTO{" + "eventoId=" + eventoId + ", asientos=" + asientos + '}'; }
}
