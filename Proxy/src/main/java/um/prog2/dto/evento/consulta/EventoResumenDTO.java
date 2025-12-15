package um.prog2.dto.evento.consulta;

import um.prog2.dto.evento.shared.EventoTipoDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO resumido para listar eventos activos en /api/endpoints/v1/eventos-resumidos.
 * Incluye sólo los campos necesarios para vista de listado.
 * Ejemplo JSON:
 * {
 *   "titulo": "Conferencia Nerd",
 *   "resumen": "Esta es una conferencia de Nerds",
 *   "descripcion": "Esta es una conferencia de prueba...",
 *   "fecha": "2025-11-10T11:00:00Z",
 *   "precioEntrada": 2500.00,
 *   "eventoTipo": { "nombre": "Conferencia", "descripcion": "Conferencia" },
 *   "id": 1
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoResumenDTO implements Serializable {

    @NotNull
    private Long id;

    @NotNull
    @Size(max = 200)
    private String titulo;

    @NotNull
    @Size(max = 500)
    private String resumen;

    private String descripcion; // LOB no necesario aquí, texto reducido

    @NotNull
    private Instant fecha;

    @NotNull
    private BigDecimal precioEntrada;

    @NotNull
    private EventoTipoDTO eventoTipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }
    public EventoTipoDTO getEventoTipo() { return eventoTipo; }
    public void setEventoTipo(EventoTipoDTO eventoTipo) { this.eventoTipo = eventoTipo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventoResumenDTO)) return false;
        EventoResumenDTO that = (EventoResumenDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "EventoResumenDTO{" +
            "id=" + id +
            ", titulo='" + titulo + '\'' +
            ", fecha=" + fecha +
            ", precioEntrada=" + precioEntrada +
            '}';
    }
}
